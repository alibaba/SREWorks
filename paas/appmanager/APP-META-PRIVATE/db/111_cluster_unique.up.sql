drop index idx_cluster_id on am_cluster;

create unique index uk_cluster_id
    on am_cluster (cluster_id);
