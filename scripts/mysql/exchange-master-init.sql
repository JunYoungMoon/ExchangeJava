-- Exchange 스키마 생성
-- 해당 명령은 'Exchange'이라는 스키마(데이터베이스)를 생성합니다.
-- 스키마가 이미 존재하는 경우 새로 생성되지 않습니다.
CREATE SCHEMA IF NOT EXISTS `Exchange` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 복제 유저 생성
-- 복제(Replication) 전용 사용자 'replication_user'를 생성합니다.
-- 이 사용자 계정은 모든 호스트('%')에서 접근 가능하며, 비밀번호는 'replication_password'입니다.
CREATE USER 'replication_user'@'%' IDENTIFIED WITH 'mysql_native_password' BY 'replication_password';

-- 권한 추가
-- 'replication_user' 사용자에게 복제(Slave) 권한을 부여합니다.
-- 이는 모든 데이터베이스와 테이블('*.*')에 대해 복제 권한을 설정합니다.
GRANT REPLICATION SLAVE ON *.* TO 'replication_user'@'%';

-- 권한 변경사항 적용
-- 권한 테이블에 대한 변경 사항을 즉시 적용합니다.
FLUSH PRIVILEGES;

-- 읽기 잠금 설정
-- Master에서 테이블이 변경되지 않도록 읽기 잠금을 설정합니다.
-- 이 잠금은 복제 설정 동안 데이터 일관성을 유지하기 위해 사용됩니다.
FLUSH TABLES WITH READ LOCK;

-- Master 상태 확인
-- 현재의 Master 상태를 표시하여 복제에 필요한 이진 로그 파일과 위치를 확인합니다.
-- SHOW MASTER STATUS;
-- File : mysql-bin.000001 (이진 로그 파일. 변경 사항이 로그되는 바이너리 형식의 파일로, 데이터베이스의 변경 내용을 기록)
-- Position : 123 (이진 로그 파일에서의 마지막 로그 항목의 위치를 나타낸다.)

-- 외부에서 접근할 유저 권한 생성
-- create user '유저명'@'%' identified by '패스워드';
-- grant all privileges on 스키마명.* to '유저명'@'%';
-- FLUSH PRIVILEGES;

-- 복제 에러시 백업 복구 방법
-- 1.mysqldump
-- Slave 중단
-- STOP SLAVE;
-- RESET MASTER;
-- RESET SLAVE ALL;

-- 마스터 컨테이너에서 데이터베이스 덤프
-- docker exec -it jun-mysql-master-1 bash
-- mysqldump -u root -p --all-databases --master-data > /tmp/master_backup.sql
-- exit

-- 덤프 파일을 로컬로 복사
-- docker cp jun-mysql-master-1:/tmp/master_backup.sql ./master_backup.sql

-- 로컬에서 슬레이브 컨테이너로 덤프 파일 복사
-- docker cp ./master_backup.sql jun-mysql-slave-1:/tmp/master_backup.sql

-- 슬레이브 컨테이너에서 데이터베이스 복원
-- docker exec -it jun-mysql-slave-1 bash
-- mysql -u root -p < /tmp/master_backup.sql
-- exit

-- 슬레이브 설정 재구성
-- CHANGE MASTER TO
-- MASTER_HOST='mysql-master',
-- MASTER_PORT=3306,
-- MASTER_USER='replication_user',
-- MASTER_PASSWORD='replication_password',
-- MASTER_AUTO_POSITION=1;
-- START SLAVE;
-- SHOW SLAVE STATUS;

-- 2.특정 GTID 파악 및 제거
-- SELECT * FROM performance_schema.replication_applier_status_by_worker;
-- SHOW SLAVE STATUS;
-- STOP SLAVE;
-- SET GLOBAL GTID_PURGED="ddac1846-bb1b-11ee-a439-0242ac120002:9";
-- START SLAVE;

-- 3. MASTER_AUTO_POSITION 꼬였을때 수동 설정
-- SHOW MASTER STATUS;
-- STOP SLAVE;
-- CHANGE MASTER TO
-- MASTER_LOG_FILE='mysql-bin.000021',
-- MASTER_LOG_POS=7098129;
-- START SLAVE;