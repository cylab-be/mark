/*
 * The MIT License
 *
 * Copyright 2020 tibo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package be.cylab.mark.activation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author tibo
 */
public final class ThreadsExecutor implements ExecutorInterface {

    private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10, 10, 0L, TimeUnit.SECONDS, queue);

    @Override
    public void submit(final Runnable job) {
        executor.submit(job);
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        executor.shutdown();
        return true;
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("executor.nodes", 1);
        map.put("executor.parallelism", executor.getMaximumPoolSize());
        map.put("executor.jobs.running", executor.getActiveCount());
        map.put("executor.jobs.executed", executor.getCompletedTaskCount());
        map.put("executor.jobs.waiting", queue.size());
        return map;
    }

}
