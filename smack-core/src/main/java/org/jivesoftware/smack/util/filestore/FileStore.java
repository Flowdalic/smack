/**
 *
 * Copyright 2018 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jivesoftware.smack.util.filestore;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class FileStore<K, V> {

    protected final File storeDirectory;

    public FileStore(File storeDirectory) {
        this.storeDirectory = storeDirectory;
    }

    protected abstract void writeValue(OutputStream os, V value);

    protected abstract V readValue(InputStream is);

    public V retrieve(K key) {
        return null;
    }

    public void store(K key, V value) {
    }

    public void emptyStore() {
    }
}
