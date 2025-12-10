# Maven 配置指南

## 一、环境变量配置

### 1. 找到Maven安装目录
假设你的Maven解压到了 `C:\Program Files\Apache\maven` 或 `D:\maven` 等位置，记住这个路径。

### 2. 配置系统环境变量

#### 步骤：
1. **打开系统属性**
   - 按 `Win + R`，输入 `sysdm.cpl`，回车
   - 或者：右键"此电脑" → "属性" → "高级系统设置"

2. **添加 MAVEN_HOME 变量**
   - 点击"环境变量"
   - 在"系统变量"区域，点击"新建"
   - 变量名：`MAVEN_HOME`
   - 变量值：你的Maven安装路径（例如：`C:\Program Files\Apache\maven`）
   - 点击"确定"

3. **配置 PATH 变量**
   - 在"系统变量"中找到 `Path` 变量
   - 点击"编辑"
   - 点击"新建"，添加：`%MAVEN_HOME%\bin`
   - 点击"确定"保存

4. **应用并关闭**
   - 所有窗口都点击"确定"关闭

### 3. 验证配置

#### 方法一：使用命令提示符
1. **重新打开命令提示符**（重要：必须重新打开才能生效）
   - 按 `Win + R`，输入 `cmd`，回车

2. **验证Maven安装**
   ```bash
   mvn -version
   ```
   
   如果看到类似以下输出，说明配置成功：
   ```
   Apache Maven 3.9.x
   Maven home: C:\Program Files\Apache\maven
   Java version: 1.8.x
   ...
   ```

#### 方法二：使用PowerShell
```powershell
mvn -version
```

## 二、配置Maven镜像（可选，推荐）

### 为什么需要配置镜像？
- 加快依赖下载速度
- 使用国内镜像源（如阿里云、腾讯云）

### 配置步骤：

1. **找到Maven的settings.xml文件**
   - 位置：`%MAVEN_HOME%\conf\settings.xml`
   - 例如：`C:\Program Files\Apache\maven\conf\settings.xml`

2. **编辑settings.xml**
   - 用记事本或任何文本编辑器打开
   - 找到 `<mirrors>` 标签（如果没有就添加）

3. **添加阿里云镜像**（推荐）
   
   在 `<mirrors>` 标签内添加：
   ```xml
   <mirror>
     <id>aliyunmaven</id>
     <mirrorOf>central</mirrorOf>
     <name>阿里云公共仓库</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```

   完整示例：
   ```xml
   <settings>
     ...
     <mirrors>
       <mirror>
         <id>aliyunmaven</id>
         <mirrorOf>central</mirrorOf>
         <name>阿里云公共仓库</name>
         <url>https://maven.aliyun.com/repository/public</url>
       </mirror>
     </mirrors>
     ...
   </settings>
   ```

4. **保存文件**

## 三、在项目中使用Maven

### 1. 进入项目目录
```bash
cd F:\HPIMS\Hospital-Pharmacy-Intelligent-Management-System\backend
```

### 2. 常用Maven命令

#### 清理项目
```bash
mvn clean
```

#### 编译项目
```bash
mvn compile
```

#### 打包项目
```bash
mvn package
```

#### 安装到本地仓库
```bash
mvn install
```

#### 下载依赖（不编译）
```bash
mvn dependency:resolve
```

#### 运行Spring Boot项目
```bash
mvn spring-boot:run
```

### 3. 首次使用建议
首次使用Maven时，建议先执行：
```bash
mvn dependency:resolve
```
这会下载所有依赖，可能需要一些时间。

## 四、常见问题

### 问题1：命令提示符显示"mvn不是内部或外部命令"
**解决方法：**
- 确认环境变量配置正确
- **重新打开**命令提示符（环境变量修改后需要重启终端）
- 检查PATH中是否包含 `%MAVEN_HOME%\bin`

### 问题2：Maven下载依赖很慢
**解决方法：**
- 配置国内镜像源（见"二、配置Maven镜像"）
- 使用阿里云或腾讯云镜像

### 问题3：Java版本不匹配
**解决方法：**
- 确保已安装JDK（建议JDK 8或更高版本）
- 配置JAVA_HOME环境变量
- 运行 `java -version` 验证Java是否安装

### 问题4：权限问题
**解决方法：**
- 如果Maven安装在系统目录，可能需要管理员权限
- 建议将Maven安装到用户目录，如 `D:\maven`

## 五、验证清单

完成配置后，请确认：

- [ ] `mvn -version` 命令可以正常执行
- [ ] 显示Maven版本信息
- [ ] 显示Java版本信息
- [ ] 显示Maven home路径
- [ ] 在项目目录可以执行 `mvn dependency:resolve`

## 六、快速测试

在项目backend目录下执行：
```bash
cd F:\HPIMS\Hospital-Pharmacy-Intelligent-Management-System\backend
mvn -version
mvn dependency:resolve
```

如果都能正常执行，说明Maven配置成功！

---

**提示：** 如果遇到问题，请检查：
1. 环境变量是否正确配置
2. 是否重新打开了命令提示符
3. Maven安装路径是否正确
4. Java是否已正确安装
