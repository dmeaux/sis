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
package org.apache.sis.internal.storage.aggregate;

import org.apache.sis.storage.Resource;


/**
 * The result of an aggregation computed by {@link CoverageAggregator}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.3
 * @since   1.3
 * @module
 */
interface AggregatedResource {
    /**
     * Sets the name of the resource.
     *
     * @param  name  new name of the resource.
     */
    void setName(String name);

    /**
     * Specifies the resource to declare in lineage metadata as the source of this resource.
     */
    void setSourceMetadata(Resource source);
}
