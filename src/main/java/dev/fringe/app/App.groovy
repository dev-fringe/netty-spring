package dev.fringe.app

import org.springframework.context.annotation.AnnotationConfigApplicationContext

class App {
	public static void main(String[] args) {
		new AnnotationConfigApplicationContext(NettyApp.class)
	}
}