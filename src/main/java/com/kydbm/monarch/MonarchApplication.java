package com.kydbm.monarch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication
 * 이 어노테이션은 세 가지 핵심적인 기능을 자동으로 설정합니다.
 * 1. @Configuration: 이 클래스가 Spring의 설정 정보를 담고 있음을 나타냅니다.
 * 2. @EnableAutoConfiguration: 클래스패스에 있는 라이브러리들을 기반으로 애플리케이션을 자동으로 구성합니다.
 * 3. @ComponentScan: 현재 패키지(`com.kydbm.monarch`) 및 하위 패키지에서 @Component, @Service 등의 컴포넌트를 찾아 Bean으로 등록합니다.
 */
@SpringBootApplication
public class MonarchApplication {

	/**
	 * 애플리케이션의 메인 메소드. 이 메소드가 실행되면서 내장 웹 서버(Tomcat)가 시작되고 Spring 애플리케이션이 구동됩니다.
	 */
	public static void main(String[] args) {
		SpringApplication.run(MonarchApplication.class, args);
	}

}
