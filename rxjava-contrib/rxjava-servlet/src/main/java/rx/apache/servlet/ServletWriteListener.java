/**
 * Copyright 2014 Jitendra Kotamraju.
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

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * A servlet {@link javax.servlet.ReadListener} that pushes data to an Observer
 *
 * @author Jitendra Kotamraju
 */
class ServletWriteListener implements WriteListener {
    private final Observer<? super Void> observer;
    private final ServletOutputStream out;

    ServletWriteListener(Observer<? super Void> observer, final ServletOutputStream out) {
        this.observer = observer;
        this.out = out;
    }

    @Override
    public void onWritePossible() {
        do {
            observer.onNext(null);
        } while(out.isReady());
    }

    @Override
    public void onError(Throwable t) {
        observer.onError(t);
    }

}
