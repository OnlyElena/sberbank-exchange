 create or alter procedure uvedom(doc_id D_ID) -- аргумент это id постановления
as
    -- описываем переменные используемые в процедуре
    -- для хранения id для таблицы ext_input_header
    declare new_id type of D_ID;
    -- универсальный уникальный идентификатор
    declare uuid varchar(40);
    -- для хранения id испол производства
    declare ip_id type of D_ID;
    --ext_info_id - идентификатор для таблицы EXT_INFORMATION
    declare ext_info_id type of D_ID;
    declare id_dbtr_name type of D_DESCRIPTION;
    declare id_dbtr_inn type of D_INN;
    declare id_dbtr_born type of D_DATE;
    declare dbtr_born_year type of D_BIRTHYEAR;
    declare restriction_int_key type of D_ID;
    declare source_barcode type of D_BARCODE;
begin
-- генерируем новый id
SELECT NEXT VALUE FOR SEQ_DOCUMENT FROM RDB$DATABASE into new_id;
-- генерируем uuid
SELECT GEN_UUID() FROM RDB$DATABASE into uuid;
-- находим id испол производства
select ip_id from O_IP where id = :doc_id into ip_id;
-- генерим новый id для ext_information
select next value for ext_information from rdb$database into ext_info_id;
select id from sendlist where sendlist_o_id = :doc_id and sendlist_contr_type containing 'Банк' into restriction_int_key;
select barcode from document where id = :doc_id into source_barcode;
-- вставляем запись в ext_input_header
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
    'EXT_REPORT',
    CAST('NOW' AS DATE),
    :source_barcode
);
-- пихаем в ext_report
insert into EXT_REPORT
(
    ID,
    IP_INTERNAL_KEY,
    RESTRICTN_INTERNAL_KEY,
    DOC_DATE,
    RESTRICTION_ANSWER_TYPE,
    DESCRIPTION
) values (
    :new_id,
    :ip_id,
    :restriction_int_key,
    cast('NOW' as date),
    3,  --2 — Указанное в постановлении имущество отсутствует
        --3 — Постановление исполнено в полном объеме
        --4 — Постановление исполнено частично
        --5 — Постановление не принято к исполнению в связи с отсутствием обязательных реквизитов постановления
        --6 — Постановление не исполнено в связи с отсутствием наложенного ограничения (ареста), указанному в постановлении о снятии ограничения (ареста)
        --7 — Постановление не исполнено в связи с отсутствием в банке должника
    'Исполнено в полном объеме'
);
end;
execute procedure uvedom(25111001805064);
drop procedure uvedom;