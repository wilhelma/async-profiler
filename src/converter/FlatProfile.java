/*
 * Copyright 2020 Andrei Pangin
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class FlatProfile extends FlameGraph {

    public FlatProfile(String... args) {
        super(args);
    }

    public void dump() throws IOException {
        if (output == null) {
            dump(System.out);
        } else {
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output), 32768);
                 PrintStream out = new PrintStream(bos, false, "UTF-8")) {
                dump(out);
            }
        }
    }

    public void dump(PrintStream out) {
        out.println(HEADER);
        printFrame(out, "all", root);
    }

    private void printFrame(PrintStream out, String title, Frame frame) {
        int type = frameType(title);
        title = stripSuffix(title);
        if (title.indexOf('\\') >= 0) {
            title = title.replace("\\", "\\\\");
        }
        if (title.indexOf('\'') >= 0) {
            title = title.replace("'", "\\'");
        }

        out.println(title + ";" + type + ";" + frame.total + ";" + frame.self);

        for (Map.Entry<String, Frame> e : frame.entrySet()) {
            Frame child = e.getValue();
            if (child.total >= mintotal) {
                printFrame(out, e.getKey(), child);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FlatProfile fp = new FlatProfile(args);
        if (fp.input == null) {
            System.out.println("Usage: java " + FlatProfile.class.getName() + " [options] input.collapsed [output.csv]");
            System.out.println();
            System.out.println("Options:");
            System.out.println("  --reverse");
            System.out.println("  --minwidth PERCENT");
            System.out.println("  --skip FRAMES");
            System.exit(1);
        }

        fp.parse();
        fp.dump();
    }
    private static final String HEADER = "function;type;total;self";
}
