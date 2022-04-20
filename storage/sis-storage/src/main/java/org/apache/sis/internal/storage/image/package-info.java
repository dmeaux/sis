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
 * Bridges between Apache SIS data stores and Java Image I/O.
 * This package provides {@link org.apache.sis.storage.DataStore} implementations wrapping
 * {@link javax.imageio.ImageReader} and {@link javax.imageio.ImageWriter} instances.
 * The data stores delegate the reading and writing of pixel values to the wrapped reader or writer,
 * completed with an additional source of information for georeferencing the image.
 * A commonly-used convention is the <cite>World File</cite> format.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.2
 *
 * @see <a href="https://en.wikipedia.org/wiki/World_file">World File format description on Wikipedia</a>
 *
 * @since 1.2
 * @module
 */
package org.apache.sis.internal.storage.image;
