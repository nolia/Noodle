package com.noodle.util

import java.util.concurrent.CountDownLatch;

class ThreadUtils {

  static CountDownLatch spawnThreads(int n, Closure action) {
    def latch = new CountDownLatch(n)
    def random = new Random()

    n.times { number ->
      new Thread({
        Thread.sleep(random.nextInt(51) + 50)
        print "\nRunning thread: $number"
        action(number)
        latch.countDown()
      }).start()
    }

    return latch
  }
}