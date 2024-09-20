package com.ittovative.schedulingbatchprocessing.config;

import com.ittovative.schedulingbatchprocessing.model.Person;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Locale;

@Configuration
public class BatchConfig {

    @Bean
    public FlatFileItemReader<Person> flatItemReader(){
        return new FlatFileItemReaderBuilder<Person>()
                .saveState(true)
                .name("file-item-reader1")
                .resource(new FileSystemResource("persons.csv"))
                .delimited()
                .names("name","age")
                .targetType(Person.class)
                .build();
    }

    @Bean
    public Job dummyJob(JobRepository jobRepository,
                        PlatformTransactionManager platformTransactionManager){
        return new JobBuilder("file-converter-job1",jobRepository)
                .start(dummyStep(jobRepository,platformTransactionManager))
                .build();
    }

    @Bean
    public Step dummyStep(JobRepository jobRepository,
                          PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("file-converter-step1",jobRepository)
                .<Person,Person>chunk(10,platformTransactionManager)
                .reader(flatItemReader())
                .processor(itemProcessor())
                .writer(flatFileItemWriter())
                .build();
    }

    @Bean
    public ItemProcessor<Person,Person> itemProcessor(){
        return item -> {
            System.out.println(item.getName() + " being processed!");
            item.setName(item.getName().toLowerCase(Locale.ROOT));
            Thread.sleep(1000);
            return item;
        };
    }

    @Bean
    public FlatFileItemWriter<Person> flatFileItemWriter(){
        return new FlatFileItemWriterBuilder<Person>()
                .name("csv-item-writer1")
                .append(true)
                .saveState(true)
                .delimited()
                .names("name","age")
                .resource(new FileSystemResource("new_persons.csv"))
                .build();
    }
}