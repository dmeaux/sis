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
 * Gradle plugin for building Apache SIS.
 * This plugin assumes that the code is organized according Module Source Hierarchy.
 * Some conventions on directory names and a few module names are hard-coded in the
 * {@link org.apache.sis.buildtools.gradle.Conventions} class.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Quentin Bialota (Geomatys)
 */
package org.apache.sis.buildtools.gradle;