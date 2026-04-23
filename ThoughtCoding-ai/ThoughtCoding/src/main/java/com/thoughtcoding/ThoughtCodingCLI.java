package com.thoughtcoding;

import com.thoughtcoding.cli.ThoughtCodingCommand;
import com.thoughtcoding.cli.SessionCommand;
import com.thoughtcoding.cli.ConfigCommand;
import com.thoughtcoding.core.ThoughtCodingContext;
import picocli.CommandLine;

/**
 * 创建整个应用的根上下文，作为所有组件的容器
 *
 * 调用 initialize() 方法加载配置、注册工具、连接MCP服务器
 *
 * 建立命令解析框架，为后续的命令路由做准备
 */

/**lqq
 * 项目的主程序没有 @SpringBootApplication 注解
 * 原因：它本质是一个「纯 Java 命令行程序」，而非「Spring Boot 后端 Web 应用」
 *      不需要 Spring 容器、不需要处理 HTTP 请求，
 *      只用了 Java 基础 + Picocli 解析命令，所以完全不需要这个注解
 */

/**TODO:项目改造成带 @SpringBootApplication 的 Web 项目
 * 1.添加 Spring Boot 核心依赖
 * 2.创建 Spring Boot 启动类（核心改造）
 * 3.新增 Controller 层，提供 HTTP 接口
 * 4.保留原有 CLI 能力
 */

public class ThoughtCodingCLI {
    /**
     * main 方法是 Java 程序的唯一入口，
     * JVM 启动时会自动找这个格式的方法执行；
     * String[] args 是用来接收程序运行时传入的命令行参数的数组。
     */
    public static void main(String[] args) {
        // 设置默认异常处理
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("💥 发生未预期错误: " + throwable.getMessage());
            System.exit(1);
        });

        // 创建并初始化应用上下文
        ThoughtCodingContext context = ThoughtCodingContext.initialize();

        // 设置Picocli命令解析器，注册所有命令
        CommandLine commandLine = new CommandLine(new ThoughtCodingCommand(context));
        commandLine.addSubcommand("session", new SessionCommand(context));
        commandLine.addSubcommand("config", new ConfigCommand(context));

        // 执行命令解析和路由
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}