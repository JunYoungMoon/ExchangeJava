-- Slave 설정 명령
-- Slave 서버에서 Master 서버를 지정하는 설정 명령입니다.
-- 복제할 Master 서버의 호스트, 포트, 사용자 이름, 비밀번호를 설정하고,
-- GTID 기반 자동 포지셔닝을 활성화합니다.
CHANGE MASTER TO
    MASTER_HOST='mysql-master',
    MASTER_PORT=3306,
    MASTER_USER='replication_user',
    MASTER_PASSWORD='replication_password',
    MASTER_AUTO_POSITION=1;

-- Slave 시작
-- 설정된 복제를 시작합니다.
START SLAVE;