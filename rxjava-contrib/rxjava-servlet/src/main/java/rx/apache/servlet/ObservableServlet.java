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

import rx.Observable;
import rx.Observable.*;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func2;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.servlet.*;

/**
 * An {@link Observable} interface to Servlet API
 *
 * @author Jitendra Kotamraju
 */
public class ObservableServlet {

    public static Observable<ByteBuffer> create(final ServletInputStream in) {
        return Observable.create(new OnSubscribeFunc<ByteBuffer>() {
            @Override
            public Subscription onSubscribe(final Observer<? super ByteBuffer> observer) {
                ReadListener listener = new ServletReadListener(in, observer);
                in.setReadListener(listener);
                return new Subscription() {
                    @Override
                    public void unsubscribe() {
                        //throw new UnsupportedOperationException();
                    }
                };
            }
        });
    }

    public static Observable<Void> create(final ServletOutputStream out) {
        return Observable.create(new OnSubscribeFunc<Void>() {
            @Override
            public Subscription onSubscribe(final Observer<? super Void> observer) {
                final ServletWriteListener listener = new ServletWriteListener(observer, out);
                out.setWriteListener(listener);
                return new Subscription() {
                    @Override
                    public void unsubscribe() {
                        listener.unsubscribe();
                    }
                };
            }
        });
    }

    public static Observable<Void> write(final Observable<ByteBuffer> data, final ServletOutputStream out) {
        return Observable.create(new OnSubscribeFunc<Void>() {
            @Override
            public Subscription onSubscribe(Observer<? super Void> t1) {
                Observable<Void> events = create(out);
                Observable<Void> writeobs = Observable.zip(data, events, new Func2<ByteBuffer, Void, Void>() {
                    @Override
                    public Void call(ByteBuffer byteBuffer, Void aVoid) {
                        try {
                            byte[] b = new byte[byteBuffer.remaining()];
                            byteBuffer.get(b);
                            System.out.println("writing byte[]");
                            out.write(b);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        return null;
                    }
                });
                return writeobs.subscribe(t1);
            }
        });
    }

}
