alter table am_app_component
    drop key uk_unique;

alter table am_app_component
    add constraint uk_unique
        unique (app_id, category, component_type, component_name, namespace_id, stage_id);
