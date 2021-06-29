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

package org.apache.dolphinscheduler.data.quality;

/**
 * Constants
 */
public final class Constants {

    private Constants() {
        throw new IllegalStateException("Construct Constants");
    }

    public static final String DATABASE = "database";

    public static final String TABLE = "table";

    public static final String URL = "url";

    public static final String USER = "user";

    public static final String PASSWORD = "password";

    public static final String DRIVER = "driver";

    public static final String DEFAULT_DRIVER = "com.mysql.jdbc.Driver";

    public static final String DEFAULT_DATABASE = "default";

    public static final String EMPTY = "";

    public static final String SQL = "sql";

    public static final String DOTS = ".";

    public static final String INPUT_TABLE = "input_table";

    public static final String OUTPUT_TABLE = "output_table";

    public static final String TMP_TABLE = "tmp_table";

}
