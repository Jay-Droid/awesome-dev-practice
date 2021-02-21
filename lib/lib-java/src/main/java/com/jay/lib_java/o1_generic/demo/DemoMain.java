package com.jay.lib_java.o1_generic.demo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jay.lib_java.o1_generic.demo.demo_test.CommonClass;
import com.jay.lib_java.o1_generic.demo.type_test.ParameterizedTypeTest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 演示Java范型机制的测试类
 *
 * @author wangxuejie
 * @version 1.0
 * @date 2019-11-26 17:51
 */
public class DemoMain {

  public static void main(String[] args) throws NoSuchFieldException {

    int demoIndex = 1;

    switch (demoIndex) {
      case 1:
        {
          // 泛型的来源-测试Java Type体系
          Demo1();
          break;
        }
      case 2:
        {
          //
          Demo2();
          break;
        }
      case 3:
        {
          //
          Demo3();
          break;
        }
      case 4:
        {
          //
          Demo4();
          break;
        }
    }
  }

  /**
   * Demo1: 测试Java-Type体系 Type体系中类型的包括：
   *
   * <p>原始类型(RawType)(原始类型不仅仅包含我们平常所指的类，还包括枚举、数组、注解等,还有基本类型即int,float,double等
   * 泛型数组类型(GenericArrayType): 表示一种元素类型是参数化类型或者类型变量的数组类型，如 GenericClass []；
   * 参数化类型(ParameterizedType): 就是用了泛型的类，如 List<T>,Map<String,Integer>； 泛型变量(TypeVariable):
   * 是各种类型变量的公共高级接口，如 T,K,V 泛型通配符类型(WildcardType): 通配符类型, 如 <?>, <? extends T> ；
   */
  private static void Demo1() throws NoSuchFieldException {
    ParameterizedTypeTest<String> parameterizedTypeTest = new ParameterizedTypeTest<>();
    System.out.println("---- 参数化类型(ParameterizedType)");
    parameterizedTypeTest.testParameterizedType();

    //    GenericArrayTypeTest<String> genericArrayTypeTest = new GenericArrayTypeTest<>();
    //    System.out.println("---- 泛型数组类型(GenericArrayType)");
    //    genericArrayTypeTest.testGenericArrayType();
    //
    //    TypeVariableTest<Integer> typeVariableTest = new TypeVariableTest<>();
    //    System.out.println("---- 泛型变量类型(TypeVariable)");
    //    typeVariableTest.testTypeVariable();
    //
    //    WildcardTypeTest wildcardTypeTest = new WildcardTypeTest();
    //    System.out.println("---- 泛型通配符类型(WildcardType)");
    //    wildcardTypeTest.testWildcardType();
  }

  /** Demo2: 测试 泛型类/泛型接口/泛型方法 */
  private static void Demo2() {}

  /** Demo3: */
  private static void Demo3() {}

  /** Demo4: */
  private static void Demo4() {
    Gson gson = new Gson();
    List<CommonClass> commonClassList = new ArrayList<>();
    commonClassList.add(new CommonClass("A"));
    commonClassList.add(new CommonClass("B"));
    commonClassList.add(new CommonClass("C"));
    String jsonStr = gson.toJson(commonClassList);
    System.out.println("jsonStr:" + jsonStr);
    Type type = new TypeToken<List<CommonClass>>() {}.getType();
    commonClassList = gson.fromJson(jsonStr.replace("A", "B"), type);
    System.out.println("commonClassList:" + commonClassList.toString());
  }
}
