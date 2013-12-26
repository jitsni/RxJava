/**
 * Copyright 2013 Jitendra Kotamraju.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.apache.servlet;

import rx.Observer;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A servlet {@link ReadListener} that pushes data to an Observer
 *
 * @author Jitendra Kotamraju
 */
class ServletReadListener implements ReadListener {
    private final Observer<? super ByteBuffer> observer;
    private final ServletInputStream in;

    ServletReadListener(ServletInputStream in, Observer<? super ByteBuffer> observer) {
        this.in = in;
        this.observer = observer;
    }

    @Override
    public void onDataAvailable() throws IOException {
        do {
            byte[] buf = new byte[4096];
            int len = in.read(buf);
            if (len != -1) {
                observer.onNext(ByteBuffer.wrap(buf, 0, len));
            }
        } while(in.isReady());
        // If isReady() false, container will call onDataAvailable()
        // when data is available.
    }

    @Override
    public void onAllDataRead() {
        observer.onCompleted();
    }

    @Override
    public void onError(Throwable t) {
        observer.onError(t);
    }
}
