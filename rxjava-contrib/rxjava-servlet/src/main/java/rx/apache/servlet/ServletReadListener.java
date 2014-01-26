/**
 * Copyright 2013-2014 Jitendra Kotamraju.
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A servlet {@link ReadListener} that pushes HTTP request data to an {@link Observer}
 *
 * @author Jitendra Kotamraju
 */
class ServletReadListener implements ReadListener {
    private static final Logger LOGGER = Logger.getLogger(ServletReadListener.class.getName());

    private final Observer<? super ByteBuffer> observer;
    private final ServletInputStream in;
    // Accessed by container thread, but assigned by some other thread (hence volatile field)
    private volatile boolean unsubscribed;

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
        // loop until isReady() false, otherwise container will not call onDataAvailable()
        } while(!unsubscribed && in.isReady());
        // If isReady() false, container will call onDataAvailable()
        // when data is available.
        if (!unsubscribed) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Waiting for container to notify when there is HTTP request data");
            }
        }
    }

    @Override
    public void onAllDataRead() {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Read all the data from ServletInputStream");
        }
        if (!unsubscribed) {
            observer.onCompleted();
        }
    }

    @Override
    public void onError(Throwable t) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Error while reading the data from ServletInputStream");
        }
        if (!unsubscribed) {
            observer.onError(t);
        }
    }

    void unsubscribe() {
        unsubscribed = true;
    }
}
