package com.cps.mybatis.reflection;

import com.cps.mybatis.reflection.invoker.GetFieldInvoker;
import com.cps.mybatis.reflection.invoker.Invoker;
import com.cps.mybatis.reflection.invoker.MethodInvoker;
import com.cps.mybatis.reflection.invoker.SetFieldInvoker;
import com.cps.mybatis.reflection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cps
 * @description: 反射器解耦对象
 * @date 2024/1/20 09:14
 * @OtherDescription: 主要用于解藕对象信息的，例如属性、方法以及关联的类都以此解析出来
 */
public class Reflector {

    //是否缓存反射出来对象
    private static boolean classCacheEnabled = true;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //线程安全的缓存
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

    private Class<?> type;

    //get 属性列表
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;
    //set 属性列表
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;

    //set 方法列表
    private Map<String, Invoker> setMethods = new HashMap<>();
    //get 方法列表
    private Map<String, Invoker> getMethods = new HashMap<>();

    //set类型列表
    private Map<String, Class<?>> setTypes = new HashMap<>();
    //get类型列表
    private Map<String, Class<?>> getTypes = new HashMap<>();

    //构造函数
    private Constructor<?> defaultConstructor;

    //记录所有属性的名称
    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    //构造器 初始化
    public Reflector(Class<?> clazz) {
        this.type = clazz;
        //加入构造函数
        addDefaultConstructor(clazz);
        //加入getter
        addGetMethods(clazz);
        //加入setter
        addSetMethods(clazz);
        //加入字段
        addFields(clazz);
        //初始化get属性列表
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        //初始化set属性列表
        writeablePropertyNames = getMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        //记录所有的属行
        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writeablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }

    //添加默认构造器
    private void addDefaultConstructor(Class<?> clazz) {
        //获得这个类所有的构造器
        Constructor<?>[] consts = clazz.getDeclaredConstructors();
        //遍历构造器
        for (Constructor<?> constructor : consts) {
            //判断是否是无参构造器
            if (constructor.getParameterTypes().length == 0) {
                //对于类中私有方法访问权限的检查
                if (canAccessPrivateMethods()) {
                    try {
                        constructor.setAccessible(true);
                    } catch (Exception ignore) {
                        //Ignored.
                    }
                }

                if (constructor.isAccessible()) {
                    this.defaultConstructor = constructor;
                }
            }
        }
    }

    /**
     * 对于类中私有方法访问权限的检查
     * SecurityManager 安全管理器检查是否
     * 允许取消由反射对象在其使用点上执行的标准 Java 语言访问检查
     * 对于 public、default（包）访问、protected、private 成员
     * =======================
     * SecurityManager
     * 安全管理器是一个允许应用程序实现安全策略的类。
     * 它允许应用程序在执行一个可能不安全或敏感的操作前确定该操作是什么，
     * 以及是否是在允许执行该操作的安全上下文中执行它。应用程序可以允许或不允许该操作
     * =======================
     * checkPermission(new ReflectPermission("suppressAccessChecks"));
     * ReflectPermission4 是一种指定权限，没有操作。
     * 当前定义的惟一名称是 suppressAccessChecks，它允许取消由反射对象在其使用点上执行的标准 Java 语言访问检查
     * - 对于 public、default（包）访问、protected、private 成员
     */
    private static boolean canAccessPrivateMethods() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    //添加get方法
    private void addGetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        //获取类中所有的方法
        Method[] methods = getClassMethods(clazz);
        //遍历所有的方法
        for (Method method : methods) {
            String name = method.getName();
            //判断方法的是不是get方法
            if (name.startsWith("get") && name.length() > 3) {
                if (method.getParameterTypes().length == 0) {
                    //通过get方法名字获取属性名字
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
            //判断方法是不是属于判断方法之类
            else if (name.startsWith("is") && name.length() > 2) {
                if (method.getParameterTypes().length == 0) {
                    //通过is方法名字获取属性名字
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    //添加set方法
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3) {
                if (method.getParameterTypes().length == 1) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    //将属性名和方法列表同时加入到集合中的，防止同一个属性对应多个方法
    private void addMethodConflict(Map<String, List<Method>> conflictingMethods, String name, Method method) {
        //computeIfAbsent方法 ： 对hashMap中指定的key值进行重新计算，如果map中不存在这个key，就加入到hashMap中; 如果key已经存在啦，就不重新计算，返回的就是后面value值
        List<Method> list = conflictingMethods.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }

    //解决setter方法冲突
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        //遍历setter集合里面的属性值
        for (String propName : conflictingSetters.keySet()) {
            //获取属性值对应的方法名
            List<Method> setters = conflictingSetters.get(propName);
            //选取第一个方法名
            Method firstMethod = setters.get(0);
            //判断该属性值的对应方法名是否唯一
            if (setters.size() == 1) {
                //如果唯一，就直接根据属性名字和方法去保存
                addSetMethod(propName, firstMethod);
            } else {
                //如果不唯一，也就是同一属性名称存在多个setter方法，则需要对比这些getter方法的返回值，选择getter方法
                //根据属性值查找其他getter方法的类型
                Class<?> expectedType = getTypes.get(propName);
                //如果找不到其他类型，说明实际上没有冲突，只有可能是方法的签名发生错误，抛出异常
                if (expectedType == null) {
                    throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                            + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredicatble results.");
                } else {
                    //如果能到其他类型，遍历setters里面所有的方法
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    //遍历查找每个方法
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        //判断是不是set方法
                        if (method.getParameterTypes().length == 1 && expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }

                    if (setter == null) {
                        //找不到，报错
                            throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                                    + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                    "specification and can cause unpredicatble results.");
                    }
                    addSetMethod(propName,setter);
                }
            }
        }
    }

    //将属性和set方法列表对应保存起来
    private void addSetMethod(String name, Method firstMethod) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(firstMethod));
            setTypes.put(name, firstMethod.getParameterTypes()[0]);
        }
    }

    //解决getter方法冲突
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (String propName : conflictingGetters.keySet()) {
            List<Method> getters = conflictingGetters.get(propName);
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();
            if (getters.size() == 1) {
                addGetMethod(propName, firstMethod);
            } else {
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();
                    if (methodType.equals(getterType)) {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    } else if (methodType.isAssignableFrom(getterType)) {
                        // OK getter type is descendant
                    } else if (getterType.isAssignableFrom(methodType)) {
                        getter = method;
                        getterType = methodType;
                    } else {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    }
                }
                addGetMethod(propName, getter);
            }
        }
    }

    //将属性和get方法列表对应保存起来
    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }

    //判断属性是否非法，属性以$开头、序列号属性、class属性都是非法
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    //获取类中所有的方法
    private Method[] getClassMethods(Class<?> cls) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            //添加所有的方法（就是将方法签名和方法以k-v的方式放入map（uniqueMethods）中）
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            //we also hava to find interface methods, because the class may be abstract
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterfaces : interfaces) {
                //添加所有接口的方法
                addUniqueMethods(uniqueMethods, anInterfaces.getDeclaredMethods());
            }

            //然后查找父类的方法，直达找不到父类为止
            currentClass = currentClass.getSuperclass();
        }
        //把方法名放到集合中
        Collection<Method> methods = uniqueMethods.values();
        //把集合放到一个数组，返回
        return methods.toArray(new Method[methods.size()]);
    }

    //将方法和对应的方法签名一一添加到所有方法集合
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            //判断方法是不是桥接方法
            if (!currentMethod.isBridge()) {
                //取得签名
                String signature = getSignature(currentMethod);

                if (!uniqueMethods.containsKey(signature)) {
                    if (canAccessPrivateMethods()) {
                        try {
                            currentMethod.setAccessible(true);
                        } catch (Exception e) {

                        }
                    }
                    //把当前的方法和对于方法的签名一起放到map中
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    //获取方法的对应签名
    /*举个例子
     * ---传入的method： public int add(int i ,int j){ return 0;}
     * ---返回的签名：int#add:int,int
     * 对于任何方法都一样
     */
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append('#');
        }

        sb.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(':');
            } else {
                sb.append(',');
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    /**
     * 添加字段
     * 1）获取类中声明的所有field属性，包含了已经解析出来的有 getter&setter 的属性。
     * 2）过滤掉已经有 getter&setter 的属性。
     */
    private void addFields(Class<?> clazz) {
        //根据类获得所有的字段
        Field[] fields = clazz.getDeclaredFields();
        //遍历所有的字段
        for (Field field : fields) {
            //设置属性私有访问权限
            if (canAccessPrivateMethods()) {
                try {
                    field.setAccessible(true);
                } catch (Exception e) {
                    //Ignored
                }
            }

            if (field.isAccessible()) {
                //属性私有,判断set方法列表里是不是有这个字段
                if (!setMethods.containsKey(field.getName())) {
                    //获取类的修饰fu
                    int modifiers = field.getModifiers();
                    //过滤掉static final的属性，这些属性只能被类加载器设置其初始值
                    if (!Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                        //添加字段
                        addSetField(field);
                    }
                }

                //同理
                if (!getMethods.containsKey(field.getName())) {
                    addGetField(field);
                }
            }
        }

        //如果还有父类的话，一样的操作
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    //添加get方法的属性
    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            getTypes.put(field.getName(), field.getType());
        }
    }

    //添加set方法的属性
    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }

    }

    //获取反射类型
    public Class<?> getType() {
        return type;
    }

    //获取默认构造器方法
    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new RuntimeException("There is no default constructor for " + type);
        }
    }

    //检查是否有默认构造器
    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }


    //根据属性名获取setter方法的返回类型
    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    //根据属性名获取对应的get方法 （通过 name 获取 getName（方法））
    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    //根据属性名获取对应的set方法
    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    //根据属性名获取属性对应的get方法的返回类型
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    //获得所有get方法的属性名字
    public String[] getGetablePropertyNames() {
        return readablePropertyNames;
    }

    public String[] getSetablePropertyNames() {
        return writeablePropertyNames;
    }

    //根据属性名判断是否有对应的set方法
    public boolean hasSetter(String propertyName) {
        return setMethods.keySet().contains(propertyName);
    }

    //根据属性名判断是否有对应的get方法
    public boolean hasGetter(String propertyName) {
        return getMethods.keySet().contains(propertyName);
    }

    //根据属性名获取属性名
    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    /*
    * 得到某个类的反射器，是静态方法，而且要缓存，又要多线程，所以REFLECTOR_MAP是一个ConcurrentHashMap
    */
    public static Reflector forClass(Class<?> clazz) {
        if (classCacheEnabled) {
            // synchronized (clazz) removed see issue #461
            // 对于每个类来说，我们假设它是不会变的，这样可以考虑将这个类的信息(构造函数，getter,setter,字段)加入缓存，以提高速度
            Reflector cached = REFLECTOR_MAP.get(clazz);
            if (cached == null) {
                cached = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz, cached);
            }
            return cached;
        } else {
            return new Reflector(clazz);
        }
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    public static boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

}


