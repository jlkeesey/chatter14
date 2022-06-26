import pl.allegro.tech.build.axion.release.domain.preRelease

/*
 * Copyright 2022 James Keesey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

plugins {
    alias(libs.plugins.axionRelease)
}

scmVersion {
    useHighestVersion = true
    versionIncrementer("incrementMinor")
    hooks {
        preRelease {
            fileUpdate {
                file("README.md")
                pattern = { previousVersion, _ -> "Stable version $previousVersion" }
                replacement = { currentVersion, _ -> "Stable version $currentVersion" }
            }
            fileUpdate {
                file("README.md")
                pattern = { previousVersion, _ -> "Stable-$previousVersion-" }
                replacement = { currentVersion, _ -> "Stable-$currentVersion-" }
            }
        }
    }
}

val applicationVersion: String by extra(scmVersion.version)

project.version = applicationVersion

//allprojects {
//    //project.version = scmVersion.version
//    //version = scmVersion.version
//}
