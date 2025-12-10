package com.huawei.aitransform.util;

/**
 * 字符串加密工具类
 * 用于生成加密后的敏感信息，方便在配置文件中使用
 * 支持加密数据库用户名、密码以及其他类型的敏感字符串（如Token、API密钥等）
 * 
 * 使用方法：
 * 1. 运行main方法
 * 2. 选择要加密的内容（用户名、密码或其他字符串）
 * 3. 输入要加密的内容
 * 4. 将输出的加密字符串（ENC(...)格式）复制到application.yml中
 */
public class PasswordEncryptor {
    
    public static void main(String[] args) {
        if (args.length >= 2) {
            // 命令行参数模式：第一个参数是类型（username/password/token等），第二个参数是要加密的内容
            String type = args[0].toLowerCase();
            String plainText = args[1];
            String encrypted = CryptoUtil.encrypt(plainText);
            
            String typeName;
            if (type.equals("username")) {
                typeName = "用户名";
            } else if (type.equals("password")) {
                typeName = "密码";
            } else {
                typeName = "字符串";
            }
            
            System.out.println("原始" + typeName + ": " + plainText);
            System.out.println("加密后: ENC(" + encrypted + ")");
            System.out.println("\n请将以下内容复制到配置文件中:");
            if (type.equals("username")) {
                System.out.println("username: ENC(" + encrypted + ")");
            } else if (type.equals("password")) {
                System.out.println("password: ENC(" + encrypted + ")");
            } else {
                System.out.println(type + ": ENC(" + encrypted + ")");
            }
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
            System.out.println("字符串加密工具");
            System.out.println("========================================");
            System.out.println("提示：可以通过环境变量 CRYPTO_KEY 设置加密密钥");
            System.out.println("     也可以通过系统属性 -Dcrypto.key=your_key 设置");
            System.out.println("     如果不设置，将使用默认密钥");
            System.out.println("========================================\n");
            
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            
            while (true) {
                System.out.print("请选择要加密的内容（1-用户名, 2-密码, 3-其他字符串, exit-退出）: ");
                String choice = scanner.nextLine().trim();
                
                if ("exit".equalsIgnoreCase(choice)) {
                    System.out.println("退出程序");
                    break;
                }
                
                String prompt;
                String fieldName;
                String typeName;
                
                if ("1".equals(choice)) {
                    prompt = "请输入要加密的用户名: ";
                    fieldName = "username";
                    typeName = "用户名";
                } else if ("2".equals(choice)) {
                    prompt = "请输入要加密的密码: ";
                    fieldName = "password";
                    typeName = "密码";
                } else if ("3".equals(choice)) {
                    prompt = "请输入要加密的字符串（如Token、API密钥等）: ";
                    fieldName = "your_field_name";
                    typeName = "字符串";
                } else {
                    System.out.println("无效的选择，请输入 1、2、3 或 exit！\n");
                    continue;
                }
                
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    System.out.println(typeName + "不能为空，请重新输入！\n");
                    continue;
                }
                
                try {
                    String encrypted = CryptoUtil.encrypt(input);
                    System.out.println("\n加密成功！");
                    System.out.println("原始" + typeName + ": " + input);
                    System.out.println("加密后: ENC(" + encrypted + ")");
                    System.out.println("\n请将以下内容复制到配置文件中:");
                    if ("3".equals(choice)) {
                        System.out.println("  # 示例：");
                        System.out.println("  # api.token: ENC(" + encrypted + ")");
                        System.out.println("  # 或");
                        System.out.println("  # your_field_name: ENC(" + encrypted + ")");
                        System.out.println("\n提示：在代码中使用 CryptoUtil.decryptIfNeeded() 方法自动解密");
                    } else {
                        System.out.println("  " + fieldName + ": ENC(" + encrypted + ")");
                    }
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

