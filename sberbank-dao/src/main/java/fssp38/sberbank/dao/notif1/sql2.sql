create or alter procedure uvedom(doc_id D_ID) -- аргумент это id постановления
as
    declare new_id type of D_ID;
    declare uuid varchar(40);
    declare ipno type of D_VARCHAR_50;
    declare source_barcode type of D_BARCODE;
begin
SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE into new_id;
SELECT GEN_UUID() FROM RDB$DATABASE into uuid;
select ipno from O_IP where id = :doc_id into ipno;
select barcode from document where id = :doc_id into source_barcode;

INSERT INTO EXT_INPUT_HEADER
(
    ID,
    PACK_NUMBER,
    PROCEED,
    AGENT_CODE,
    AGENT_DEPT_CODE,
    AGENT_AGREEMENT_CODE,
    EXTERNAL_KEY,
    METAOBJECTNAME,
    DATE_IMPORT,
    SOURCE_BARCODE
) VALUES (
    :new_id,
    0,
    0,
    'СБЕРБАНК',
    'СБЕРБАНКИРК',
    'СБЕРБАНКСОГЛ',
    :uuid,
    'EXT_RESPONSE',
    CAST('NOW' AS DATE),
    :source_barcode
);

insert into EXT_RESPONSE
(
    ID,
    RESPONSE_DATE,
    IP_NUM,
    ENTITY_NAME,
    DATA_STR,
    REQUEST_NUM
) values (
    :new_id,
    cast('now' as timestamp),
    :ipno,
    '',
    'Постановление №Такое-то принято к исполнению',
    0
);
end;
execute procedure uvedom(25111001805064);
drop procedure uvedom;