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
package org.apache.sis.buildtools.gradle;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import org.gradle.api.Project;


/**
 * Compiles Java interfaces from OpenOffice IDL files.
 *
 * <p>In an ideal world, this task would execute {@code idlc} on the {@code *.idl} files,
 * then {@code regmerge} on the generated {@code *.urd} files,
 * then {@code javamaker} on the generated {@code *.rdb} files.
 * However, since the above mentioned tools are native and would require a manual installation
 * on every developer machine, current version just copies a pre-compiled class file.
 * This copy must occurs after the compilation phase in order to overwrite the files
 * generated by {@code javac}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Quentin Bialota (Geomatys)
 */
final class JavaMaker extends SimpleFileVisitor<Path> {
    /**
     * The root of source and target directories. Files below {@code source} will be copied
     * with identical path (relative to {@code source}) under {@code target} directory.
     */
    private final Path source, target;

    /**
     * Number of files copied.
     */
    private int count;

    /**
     * Creates a new copier.
     *
     * @param unopkgDirectory  base directory of the class to "compile".
     * @param outputDirectory  directory where the output class files are located.
     */
    private JavaMaker(final File unopkgDirectory, final File outputDirectory) {
        source = unopkgDirectory.toPath();
        target = outputDirectory.toPath();
    }

    /**
     * Copies {@code *.class} files from source directory to output directory.
     * The output directory shall already exist. It should be the case if all
     * sources files have been compiled before this method is invoked.
     *
     * @param  project  the sub-project.
     */
    static void execute(final Project project) {
        final File moduleDirectory = Conventions.getBundleSourceDirectory(project, UnoPkg.MODULE);
        if (moduleDirectory.isDirectory()) try {
            File outputDirectory;                       // Path to "./build/classes/java/main/org.apache.sis.openoffice/"
            outputDirectory = Conventions.fileRelativeToBuild(project, Conventions.MAIN_CLASSES_DIRECTORY);
            outputDirectory = new File(outputDirectory, UnoPkg.MODULE);
            final var c = new JavaMaker(moduleDirectory, outputDirectory);
            Files.walkFileTree(c.source, c);
            project.getLogger().debug("[sis-unopkg] Copied " + c.count + " pre-compiled class files.");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Determines whether the given directory should be visited.
     * This method skips hidden directories.
     */
    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
        return dir.getFileName().toString().startsWith(".") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
    }

    /**
     * Invoked for a file in a directory. The destination shall already exists.
     * It should be the case if this task is invoked after Java compilation.
     */
    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        final String filename = file.getFileName().toString();
        if (filename.endsWith(".class") || filename.endsWith(".CLASS")) {
            final Path dst = target.resolve(source.relativize(file)).normalize();
            if (!dst.startsWith(target)) {
                throw new SISBuildException("Unexpected target path: " + dst);
            }
            Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
            count++;
        }
        return FileVisitResult.CONTINUE;
    }
}