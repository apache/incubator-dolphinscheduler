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
package org.apache.dolphinscheduler.api.dto.resources.visitor;


import org.apache.dolphinscheduler.api.dto.resources.Directory;
import org.apache.dolphinscheduler.api.dto.resources.FileLeaf;
import org.apache.dolphinscheduler.api.dto.resources.ResourceComponent;
import org.apache.dolphinscheduler.dao.entity.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * resource tree visitor
 */
public class ResourceTreeVisitor implements Visitor{

    /**
     * resource list
     */
    private List<Resource> resourceList;

    public ResourceTreeVisitor() {
    }

    /**
     * constructor
     * @param resourceList resource list
     */
    public ResourceTreeVisitor(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * visit
     * @return resource component
     */
    @Override
    public ResourceComponent visit() {
        ResourceComponent rootDirectory = new Directory();
        for (Resource resource : resourceList) {
            // judge whether is root node
            if (rootNode(resource)){
                ResourceComponent tempResourceComponent = getResourceComponent(resource);
                rootDirectory.add(tempResourceComponent);
                tempResourceComponent.setChildren(setChildren(tempResourceComponent.getId(),resourceList));
            }
        }
        return rootDirectory;
    }

    /**
     * set children
     * @param id    id
     * @param list  resource list
     * @return resource component list
     */
    public static List<ResourceComponent> setChildren(int id, List<Resource> list ){
        List<ResourceComponent> childList = new ArrayList<>();
        for (Resource resource : list) {
            if (id == resource.getPid()){
                ResourceComponent tempResourceComponent = getResourceComponent(resource);
                childList.add(tempResourceComponent);
            }
        }
        for (ResourceComponent resourceComponent : childList) {
            resourceComponent.setChildren(setChildren(resourceComponent.getId(),list));
        }
        if (childList.size()==0){
            return new ArrayList<>();
        }
        return childList;
    }

    /**
     * Determine whether it is the root node
     * @param resource resource
     * @return true if it is the root node
     */
    public boolean rootNode(Resource resource) {

        boolean isRootNode = true;
        if(resource.getPid() != -1 ){
            for (Resource parent : resourceList) {
                if (resource.getPid() == parent.getId()) {
                    isRootNode = false;
                    break;
                }
            }
        }
        return isRootNode;
    }

    /**
     * get resource component by resource
     * @param resource resource
     * @return resource component
     */
    private static ResourceComponent getResourceComponent(Resource resource) {
        ResourceComponent tempResourceComponent;
        if(resource.isDirectory()){
            tempResourceComponent = new Directory();
        }else{
            tempResourceComponent = new FileLeaf();
        }
        
        tempResourceComponent.setName(resource.getAlias());
        tempResourceComponent.setFullName(resource.getFullName().replaceFirst("/",""));
        tempResourceComponent.setId(resource.getId());
        tempResourceComponent.setPid(resource.getPid());
        tempResourceComponent.setIdValue(resource.getId(),resource.isDirectory());
        tempResourceComponent.setDescription(resource.getDescription());
        tempResourceComponent.setType(resource.getType());
        return tempResourceComponent;
    }

}
