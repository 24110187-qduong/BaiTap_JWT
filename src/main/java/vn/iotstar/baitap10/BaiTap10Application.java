package vn.iotstar.baitap10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import vn.iotstar.baitap10.configs.DatabaseInitializer;

@SpringBootApplication
public class BaiTap10Application {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BaiTap10Application.class);
        app.addListeners(new DatabaseInitializer());
        app.run(args);
    }
}