package org.opencdmp.filetransformer.rda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.opencdmp.filetransformerbase.*",
        "org.opencdmp.filetransformer.rda.*",
        "gr.cite.tools",
        "gr.cite.commons"
})
public class FileTransformerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileTransformerApplication.class, args);
    }
}
