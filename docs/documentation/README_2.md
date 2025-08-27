# Memoir tests

- 位置：`springboot/src/test/java/com/example/demo/memoir/`
- 内容：集成测试覆盖回忆录最小API（创建项目、添加分段、查询）。
- 说明：测试使用 MockMvc，无需外部服务；数据库使用 SQLite 本地文件，schema.sql 若不存在会被初始化。
