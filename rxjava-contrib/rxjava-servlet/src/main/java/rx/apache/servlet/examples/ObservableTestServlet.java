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
package rx.apache.servlet.examples;

import rx.Observable;
import rx.Observer;
import rx.apache.servlet.ObservableServlet;
import rx.util.functions.Func1;
import rx.util.functions.Func2;
import rx.util.functions.FuncN;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/**
 *
 * @author Jitendra Kotamraju
 */
@WebServlet(value="/test", asyncSupported = true)
public class ObservableTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(200);
        PrintWriter pw = resp.getWriter();
        pw.write("Test");
        pw.close();
    }

    @Override
    protected void doPost(HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {


        final AsyncContext ac = req.startAsync();
        ServletInputStream in = req.getInputStream();

        ObservableServlet.create(in)
            .subscribe(new Observer<ByteBuffer>() {
                @Override
                public void onCompleted() {
                    System.out.println("Read onCompleted=" + Thread.currentThread());
                    resp.setStatus(HttpServletResponse.SC_OK);

                    Observable<ByteBuffer> data = Observable.from(
                            ByteBuffer.wrap("10000000000000".getBytes()),
                            ByteBuffer.wrap("20000000000000".getBytes()),
                            ByteBuffer.wrap("30000000000000".getBytes()),
                            ByteBuffer.wrap("40000000000000".getBytes()),
                            ByteBuffer.wrap("50000000000000".getBytes()),
                            ByteBuffer.wrap("60000000000000".getBytes())
                    );
                    ServletOutputStream out = null;
                    try {
                        out = resp.getOutputStream();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    ObservableServlet.write(data, out);
                    Observable<Void> writeStatus = ObservableServlet.write(data, out);
                    writeStatus.subscribe(new Observer<Void>() {
                        @Override
                        public void onCompleted() {
                            System.out.println("Composite Write onCompleted");
                            ac.complete();
                        }

                        @Override
                        public void onError(Throwable e) {
                            System.out.println("write onError=" + Thread.currentThread());
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Void args) {
                            System.out.println("Composite Write onNext");
                            // no-op
                        }
                    });
                }

                @Override
                public void onError(Throwable e) {
                    System.out.println("read onError=" + Thread.currentThread());
                    e.printStackTrace();
                }

                @Override
                public void onNext(ByteBuffer buf) {
                    //Thread.dumpStack();
                    System.out.println("read onNext=" + Thread.currentThread());
                }
            });



    }

}
