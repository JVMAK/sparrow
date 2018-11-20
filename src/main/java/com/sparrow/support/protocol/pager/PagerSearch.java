/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.support.protocol.pager;

import com.sparrow.constant.magic.SYMBOL;

/**
 * simple pager search parameter
 *
 * @author harry
 */
public class PagerSearch extends SimplePager {

    /**
     * avoid deep pager
     *
     * @param pageSize
     */
    public PagerSearch(Integer pageSize) {
        super(0, pageSize);
    }


    public PagerSearch(Integer currentPageIndex, Integer pageSize) {
        super(currentPageIndex, pageSize);
    }


    public String getLimitClause() {
        //no page
        if (pageSize <= 0) {
            return SYMBOL.EMPTY;
        }
        Integer pageIndex = this.getCurrentPageIndex() == 0 ? 0 : this.getCurrentPageIndex() - 1;
        return " limit " + (pageIndex * this.getPageSize()) + "," + this.getPageSize();
    }
}
