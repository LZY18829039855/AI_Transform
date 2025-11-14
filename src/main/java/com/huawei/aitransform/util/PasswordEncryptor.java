package com.huawei.aitransform.util;

/**
 * 密码加密工具类
 * 用于生成加密后的密码，方便在配置文件中使用
 * 
 * 使用方法：
 * 1. 运行main方法
 * 2. 输入要加密的密码
 * 3. 将输出的加密字符串（ENC(...)格式）复制到application.yml中
 */
public class PasswordEncryptor {
    
    public static void main(String[] args) {
        if (args.length > 0) {
            // 命令行参数模式
            String plainText = args[0];
            String encrypted = CryptoUtil.encrypt(plainText);
            System.out.println("原始密码: " + plainText);
            System.out.println("加密后: ENC(" + encrypted + ")");
            System.out.println("\n请将以下内容复制到application.yml的password字段:");
            System.out.println("password: ENC(" + encrypted + ")");
        } else {
            // 交互模式
            System.out.println("========================================");
            System.out.println("数据库密码加密工具");
            System.out.println("========================================");
            System.out.println("提示：可以通过环境变量 CRYPTO_KEY 设置加密密钥");
            System.out.println("     也可以通过系统属性 -Dcrypto.key=your_key 设置");
            System.out.println("     如果不设置，将使用默认密钥");
            System.out.println("========================================\n");
            
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (true) {
                System.out.print("请输入要加密的密码（输入 'exit' 退出）: ");
                String input = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(input)) {
                    System.out.println("退出程序");
                    break;
                }
                
                if (input.isEmpty()) {
                    System.out.println("密码不能为空，请重新输入！\n");
                    continue;
                }
                
                try {
                    String encrypted = CryptoUtil.encrypt(input);
                    System.out.println("\n加密成功！");
                    System.out.println("原始密码: " + input);
                    System.out.println("加密后: ENC(" + encrypted + ")");
                    System.out.println("\n请将以下内容复制到application.yml的password字段:");
                    System.out.println("  password: ENC(" + encrypted + ")");
                    System.out.println("\n" + "=".repeat(50) + "\n");
                } catch (Exception e) {
                    System.err.println("加密失败: " + e.getMessage());
                    System.out.println();
                }
            }
            
            scanner.close();
        }
    }
}

