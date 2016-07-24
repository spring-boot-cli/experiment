package io.github.soiff.experiment

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

import java.util.concurrent.CountDownLatch;

@SpringBootApplication(exclude = [DataSourceAutoConfiguration.class])
public class ExperimentApplication {
    private static final Logger log = LoggerFactory.getLogger(ExperimentApplication.class)

    public static void main(String... args) {
        SpringApplication.run(ExperimentApplication.class, args)
    }

    @Autowired
    private Environment env;

    @Bean
    @ConditionalOnMissingBean
    public CountDownLatch getCountDownLatch () {
        return new CountDownLatch(Integer.valueOf(env.getProperty("latch.count", "5")))
    }

    public static class LatchThread extends Thread {
        private CountDownLatch latch

        LatchThread() {
        }

        LatchThread(CountDownLatch latch) {
            this.latch = latch
        }

        @Override
        synchronized void start() {
            super.start()
        }

        @Override
        void run() {
            for (StackTraceElement element : currentThread().getStackTrace())
                log.info("{} => {}", latch.count, element.toString())
            this.latch.countDown()
            if (this.latch.count <= 0)
                return
            new LatchThread(latch).start()
        }
    }

    @Bean
    public ApplicationRunner getApplicationRunner(CountDownLatch latch) {
        return new ApplicationRunner() {
            public void run(ApplicationArguments args) throws Exception {
                new LatchThread(latch).start()
                latch.await()
            }
        }
    }
}
