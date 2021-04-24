/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

    @Test
    public void equalLists() {
        Assert.assertTrue(CollectionUtils.equalLists(null,null));
        Assert.assertTrue(CollectionUtils.equalLists(new ArrayList<Integer>(),new ArrayList<Integer>()));
        List<Integer> a = new ArrayList<Integer>();
        a.add(1);
        a.add(2);
        List<Integer> b = new ArrayList<Integer>();
        b.add(1);
        b.add(2);
        Assert.assertTrue(CollectionUtils.equalLists(a, b));
        a.add(1);
        Assert.assertFalse(CollectionUtils.equalLists(a, b));
        b.add(2);
        Assert.assertFalse(CollectionUtils.equalLists(a, b));
        a.add(2);
        b.add(1);
        a.add(4);
        b.add(2);
        Assert.assertFalse(CollectionUtils.equalLists(a, b));
        Assert.assertFalse(CollectionUtils.equalLists(null, new ArrayList<Integer>()));
        Assert.assertFalse(CollectionUtils.equalLists(new ArrayList<Integer>(), null));
    }

    @Test
    public void subtract() {
        Set<Integer> a = new HashSet<Integer>();
        a.add(1);
        a.add(2);
        a.add(3);
        Set<Integer> b = new HashSet<Integer>();
        b.add(0);
        b.add(2);
        b.add(4);
        Assert.assertArrayEquals(new Integer[]{1,3},CollectionUtils.subtract(a,b).toArray());
    }

    @Test
    public void stringToMap() {
        Map<String, String> a = CollectionUtils.stringToMap("a=b;c=d;", ";");
        Assert.assertNotNull(a);
        Assert.assertTrue(a.size() == 2);
        a = CollectionUtils.stringToMap(null, ";");
        Assert.assertTrue(a.isEmpty());
        a = CollectionUtils.stringToMap("", ";");
        Assert.assertTrue(a.isEmpty());
        a = CollectionUtils.stringToMap("a=b;c=d", "");
        Assert.assertTrue(a.isEmpty());
        a = CollectionUtils.stringToMap("a=b;c=d", null);
        Assert.assertTrue(a.isEmpty());
        a = CollectionUtils.stringToMap("a=b;c=d;e=f", ";");
        Assert.assertEquals(3, a.size());
        a = CollectionUtils.stringToMap("a;b=f", ";");
        Assert.assertTrue(a.isEmpty());
        a = CollectionUtils.stringToMap("a=b;c=d;e=f;", ";", "test");
        Assert.assertEquals(3, a.size());
        Assert.assertNotNull(a.get("testa"));
    }

    @Test
    public void getListByExclusion() {
        Assert.assertNotNull(CollectionUtils.getListByExclusion(null, null));
        List<Integer> originList = new ArrayList<>();
        originList.add(1);
        originList.add(2);
        List<Map<String, Object>> ret = CollectionUtils.getListByExclusion(originList, null);
        Assert.assertEquals(2, ret.size());
        ret = CollectionUtils.getListByExclusion(originList, new HashSet<>());
        Assert.assertEquals(2, ret.size());
        Assert.assertFalse(ret.get(0).isEmpty());
        Set<String> exclusion = new HashSet<>();
        exclusion.add(Constants.CLASS);
        ret = CollectionUtils.getListByExclusion(originList, exclusion);
        Assert.assertEquals(2, ret.size());
        Assert.assertTrue(ret.get(0).isEmpty());
    }

    @Test
    public void isNotEmpty() {
        List<Integer> list = new ArrayList<>();
        Assert.assertFalse(CollectionUtils.isNotEmpty(list));
        Assert.assertFalse(CollectionUtils.isNotEmpty(null));
    }
    @Test
    public void isEmpty(){
        List<Integer> list = new ArrayList<>();
        Assert.assertTrue(CollectionUtils.isEmpty(list));
        Assert.assertTrue(CollectionUtils.isEmpty(null));
        list.add(1);
        Assert.assertFalse(CollectionUtils.isEmpty(list));
    }
    @Test
    public void isEqualCollection() {
        List<Integer> a = new ArrayList<>();
        a.add(1);
        List<Integer> b = new ArrayList<>();
        b.add(1);
        Assert.assertTrue(CollectionUtils.isEqualCollection(a,b));
        b.add(2);
        Assert.assertFalse(CollectionUtils.isEqualCollection(a,b));
    }

    @Test
    public void getCardinalityMap(){
        List<Integer> a = new ArrayList<>();
        a.add(1);
        a.add(2);
        a.add(2);
        a.add(3);
        a.add(3);
        a.add(3);
        Map<Integer,Integer> cardinalityMap = CollectionUtils.getCardinalityMap(a);
        Assert.assertEquals(3, cardinalityMap.size());
        Assert.assertEquals(1, cardinalityMap.get(1).intValue());
        Assert.assertEquals(2, cardinalityMap.get(2).intValue());
        Assert.assertEquals(3, cardinalityMap.get(3).intValue());
    }

    @Test
    public void transformToList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        List<Integer> integers = CollectionUtils.transformToList(stringList, String::length);
        Assert.assertFalse(integers.isEmpty());
    }

    @Test
    public void collectionToMap() {
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        Map<Integer, String> lengthStringMap = CollectionUtils.collectionToMap(stringList, String::length);
        Assert.assertFalse(lengthStringMap.isEmpty());
    }
}
