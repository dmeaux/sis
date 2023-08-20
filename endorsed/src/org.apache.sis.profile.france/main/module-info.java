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

/**
 * French profile of metadata.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Guilhem Legal (Geomatys)
 * @version 1.4
 * @since   0.4
 */
module org.apache.sis.profile.france {
    requires jakarta.xml.bind;
    requires transitive org.apache.sis.metadata;

    provides org.apache.sis.internal.jaxb.TypeRegistration
        with org.apache.sis.xml.bind.fra.ProfileTypes;

    exports org.apache.sis.profile.france;

    opens org.apache.sis.xml.bind.fra to jakarta.xml.bind;
}
