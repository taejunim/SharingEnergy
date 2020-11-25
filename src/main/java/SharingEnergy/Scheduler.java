package SharingEnergy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;


public class Scheduler {

    Logger logger = LogManager.getLogger(Scheduler.class);

    /*매월 1일 0시 30분 명예의전당 입력*/
    @Scheduled(cron="*/10 * * * * *")
    public void HofScheduler() {
        try{

            logger.info("schedule Test : " + System.currentTimeMillis());



        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
