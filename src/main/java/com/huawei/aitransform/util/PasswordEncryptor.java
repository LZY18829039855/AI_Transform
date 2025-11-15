package com.huawei.aitransform.util;

/**
 * 数据库账号密码加密工具类
 * 用于生成加密后的用户名和密码，方便在配置文件中使用
 * 
 * 使用方法：
 * 1. 运行main方法
 * 2. 选择要加密的内容（用户名或密码）
 * 3. 输入要加密的内容
 * 4. 将输出的加密字符串（ENC(...)格式）复制到application.yml中
 */
public class PasswordEncryptor {
    
    public static void main(String[] args) {
        if (args.length >= 2) {
            // 命令行参数模式：第一个参数是类型（username/password），第二个参数是要加密的内容
            String type = args[0].toLowerCase();
            String plainText = args[1];
            String encrypted = CryptoUtil.encrypt(plainText);
            System.out.println("原始" + (type.equals("username") ? "用户名" : "密码") + ": " + plainText);
            System.out.println("加密后: ENC(" + encrypted + ")");
            System.out.println("\n请将以下内容复制到application.yml的" + type + "字段:");
            System.out.println((type.equals("username") ? "username" : "password") + ": ENC(" + encrypted + ")");
        } else if (args.length == 1) {
            // 单个参数模式：只加密密码（向后兼容）
            String plainText = args[0];
            String encrypted = CryptoUtil.encrypt(plainText);
            System.out.println("原始密码: " + plainText);
            System.out.println("加密后: ENC(" + encrypted + ")");
            System.out.println("\n请将以下内容复制到application.yml的password字段:");
            System.out.println("password: ENC(" + encrypted + ")");
        } else {
            // 交互模式
            System.out.println("========================================");
            System.out.println("数据库账号密码加密工具");
            System.out.println("========================================");
            System.out.println("提示：可以通过环境变量 CRYPTO_KEY 设置加密密钥");
            System.out.println("     也可以通过系统属性 -Dcrypto.key=your_key 设置");
            System.out.println("     如果不设置，将使用默认密钥");
            System.out.println("========================================\n");
            
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (true) {
                System.out.print("请选择要加密的内容（1-用户名, 2-密码, exit-退出）: ");
                String choice = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(choice)) {
                    System.out.println("退出程序");
                    break;
                }
                
                String type;
                String prompt;
                String fieldName;
                
                if ("1".equals(choice)) {
                    type = "username";
                    prompt = "请输入要加密的用户名: ";
                    fieldName = "username";
                } else if ("2".equals(choice)) {
                    type = "password";
                    prompt = "请输入要加密的密码: ";
                    fieldName = "password";
                } else {
                    System.out.println("无效的选择，请输入 1、2 或 exit！\n");
                    continue;
                }
                
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println((type.equals("username") ? "用户名" : "密码") + "不能为空，请重新输入！\n");
                    continue;
                }
                
                try {
                    String encrypted = CryptoUtil.encrypt(input);
                    System.out.println("\n加密成功！");
                    System.out.println("原始" + (type.equals("username") ? "用户名" : "密码") + ": " + input);
                    System.out.println("加密后: ENC(" + encrypted + ")");
                    System.out.println("\n请将以下内容复制到application.yml的" + fieldName + "字段:");
                    System.out.println("  " + fieldName + ": ENC(" + encrypted + ")");
                    System.out.println("\n" + repeatString("=", 50) + "\n");
                } catch (Exception e) {
                    System.err.println("加密失败: " + e.getMessage());
                    System.out.println();
                }
            }
            
            scanner.close();
        }
    }
    
    /**
     * 重复字符串（Java 8 兼容方法）
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private static String repeatString(String str, int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}

