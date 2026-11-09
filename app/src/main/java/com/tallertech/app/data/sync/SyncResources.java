package com.tallertech.app.data.sync;

import com.tallertech.app.data.local.TallerTechDatabase;
import com.tallertech.app.data.local.entity.*;
import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.sync.mapper.*;
import java.io.IOException;
import java.util.*;

final class SyncResources {
    private SyncResources() {}
    static List<SyncResource<?>> ordered(TallerTechDatabase db, TallerTechApi api) {
        return Arrays.asList(client(db, api), equipment(db, api), serviceOrder(db, api), diagnosis(db, api), repair(db, api));
    }

    private static SyncResource<ClientEntity> client(TallerTechDatabase db, TallerTechApi api) {
        return new SyncResource<ClientEntity>("Client") {
            String id(ClientEntity e) { return e.getId(); }
            long time(ClientEntity e) { return e.getUpdatedAt(); }
            String status(ClientEntity e) { return e.getSyncStatus(); }
            ClientEntity local(String id) { return db.clientDao().getById(id); }
            List<ClientEntity> pending() { return db.clientDao().getPending(); }
            void insert(ClientEntity e) { db.clientDao().insert(e); }
            void update(ClientEntity e) { db.clientDao().update(e); }
            void mark(String id, long time, String status) { db.clientDao().setSyncStatus(id, time, status); }
            List<ClientEntity> download() throws IOException {
                List<ClientEntity> result = new ArrayList<>();
                for (var dto : SyncHttp.body(api.getClientList())) result.add(ClientMapper.entity(dto));
                return result;
            }
            ClientEntity remote(String id) throws IOException {
                var dto = SyncHttp.optional(api.getClient(id));
                return dto == null ? null : ClientMapper.entity(dto);
            }
            ClientEntity upload(ClientEntity e, boolean exists) throws IOException {
                var request = ClientMapper.request(e);
                return ClientMapper.entity(SyncHttp.body(exists
                        ? api.updateClient(e.getId(), request) : api.createClient(request)));
            }
        };
    }

    private static SyncResource<EquipmentEntity> equipment(TallerTechDatabase db, TallerTechApi api) {
        return new SyncResource<EquipmentEntity>("Equipment") {
            String id(EquipmentEntity e) { return e.getId(); }
            long time(EquipmentEntity e) { return e.getUpdatedAt(); }
            String status(EquipmentEntity e) { return e.getSyncStatus(); }
            EquipmentEntity local(String id) { return db.equipmentDao().getById(id); }
            List<EquipmentEntity> pending() { return db.equipmentDao().getPending(); }
            void insert(EquipmentEntity e) { db.equipmentDao().insert(e); }
            void update(EquipmentEntity e) { db.equipmentDao().update(e); }
            void mark(String id, long time, String status) { db.equipmentDao().setSyncStatus(id, time, status); }
            List<EquipmentEntity> download() throws IOException {
                List<EquipmentEntity> result = new ArrayList<>();
                for (var dto : SyncHttp.body(api.getEquipmentList())) result.add(EquipmentMapper.entity(dto));
                return result;
            }
            EquipmentEntity remote(String id) throws IOException {
                var dto = SyncHttp.optional(api.getEquipment(id));
                return dto == null ? null : EquipmentMapper.entity(dto);
            }
            EquipmentEntity upload(EquipmentEntity e, boolean exists) throws IOException {
                var request = EquipmentMapper.request(e);
                return EquipmentMapper.entity(SyncHttp.body(exists
                        ? api.updateEquipment(e.getId(), request) : api.createEquipment(request)));
            }
            boolean parentsReady(EquipmentEntity e) {
                var parent = db.clientDao().getById(e.getClientId());
                return parent != null && SyncStatus.SYNCED.equals(parent.getSyncStatus());
            }
        };
    }

    private static SyncResource<ServiceOrderEntity> serviceOrder(TallerTechDatabase db, TallerTechApi api) {
        return new SyncResource<ServiceOrderEntity>("ServiceOrder") {
            String id(ServiceOrderEntity e) { return e.getId(); }
            long time(ServiceOrderEntity e) { return e.getUpdatedAt(); }
            String status(ServiceOrderEntity e) { return e.getSyncStatus(); }
            ServiceOrderEntity local(String id) { return db.serviceOrderDao().getById(id); }
            List<ServiceOrderEntity> pending() { return db.serviceOrderDao().getPending(); }
            void insert(ServiceOrderEntity e) { db.serviceOrderDao().insert(e); }
            void update(ServiceOrderEntity e) { db.serviceOrderDao().update(e); }
            void mark(String id, long time, String status) { db.serviceOrderDao().setSyncStatus(id, time, status); }
            List<ServiceOrderEntity> download() throws IOException {
                List<ServiceOrderEntity> result = new ArrayList<>();
                for (var dto : SyncHttp.body(api.getServiceOrderList())) result.add(ServiceOrderMapper.entity(dto));
                return result;
            }
            ServiceOrderEntity remote(String id) throws IOException {
                var dto = SyncHttp.optional(api.getServiceOrder(id));
                return dto == null ? null : ServiceOrderMapper.entity(dto);
            }
            ServiceOrderEntity upload(ServiceOrderEntity e, boolean exists) throws IOException {
                var request = ServiceOrderMapper.request(e);
                return ServiceOrderMapper.entity(SyncHttp.body(exists
                        ? api.updateServiceOrder(e.getId(), request) : api.createServiceOrder(request)));
            }
            boolean parentsReady(ServiceOrderEntity e) {
                var client = db.clientDao().getById(e.getClientId());
                var equipment = db.equipmentDao().getById(e.getEquipmentId());
                return client != null && equipment != null && SyncStatus.SYNCED.equals(client.getSyncStatus())
                        && SyncStatus.SYNCED.equals(equipment.getSyncStatus());
            }
        };
    }

    private static SyncResource<DiagnosisEntity> diagnosis(TallerTechDatabase db, TallerTechApi api) {
        return new SyncResource<DiagnosisEntity>("Diagnosis") {
            String id(DiagnosisEntity e) { return e.getId(); }
            long time(DiagnosisEntity e) { return e.getUpdatedAt(); }
            String status(DiagnosisEntity e) { return e.getSyncStatus(); }
            DiagnosisEntity local(String id) { return db.diagnosisDao().getById(id); }
            List<DiagnosisEntity> pending() { return db.diagnosisDao().getPending(); }
            void insert(DiagnosisEntity e) { db.diagnosisDao().insert(e); }
            void update(DiagnosisEntity e) { db.diagnosisDao().update(e); }
            void mark(String id, long time, String status) { db.diagnosisDao().setSyncStatus(id, time, status); }
            List<DiagnosisEntity> download() throws IOException {
                List<DiagnosisEntity> result = new ArrayList<>();
                for (var dto : SyncHttp.body(api.getDiagnosisList())) result.add(DiagnosisMapper.entity(dto));
                return result;
            }
            DiagnosisEntity remote(String id) throws IOException {
                var dto = SyncHttp.optional(api.getDiagnosis(id));
                return dto == null ? null : DiagnosisMapper.entity(dto);
            }
            DiagnosisEntity upload(DiagnosisEntity e, boolean exists) throws IOException {
                var request = DiagnosisMapper.request(e);
                return DiagnosisMapper.entity(SyncHttp.body(exists
                        ? api.updateDiagnosis(e.getId(), request) : api.createDiagnosis(request)));
            }
            boolean parentsReady(DiagnosisEntity e) {
                var parent = db.serviceOrderDao().getById(e.getOrderId());
                return parent != null && SyncStatus.SYNCED.equals(parent.getSyncStatus());
            }
        };
    }

    private static SyncResource<RepairEntity> repair(TallerTechDatabase db, TallerTechApi api) {
        return new SyncResource<RepairEntity>("Repair") {
            String id(RepairEntity e) { return e.getId(); }
            long time(RepairEntity e) { return e.getUpdatedAt(); }
            String status(RepairEntity e) { return e.getSyncStatus(); }
            RepairEntity local(String id) { return db.repairDao().getById(id); }
            List<RepairEntity> pending() { return db.repairDao().getPending(); }
            void insert(RepairEntity e) { db.repairDao().insert(e); }
            void update(RepairEntity e) { db.repairDao().update(e); }
            void mark(String id, long time, String status) { db.repairDao().setSyncStatus(id, time, status); }
            List<RepairEntity> download() throws IOException {
                List<RepairEntity> result = new ArrayList<>();
                for (var dto : SyncHttp.body(api.getRepairList())) result.add(RepairMapper.entity(dto));
                return result;
            }
            RepairEntity remote(String id) throws IOException {
                var dto = SyncHttp.optional(api.getRepair(id));
                return dto == null ? null : RepairMapper.entity(dto);
            }
            RepairEntity upload(RepairEntity e, boolean exists) throws IOException {
                var request = RepairMapper.request(e);
                return RepairMapper.entity(SyncHttp.body(exists
                        ? api.updateRepair(e.getId(), request) : api.createRepair(request)));
            }
            boolean parentsReady(RepairEntity e) {
                var parent = db.serviceOrderDao().getById(e.getOrderId());
                return parent != null && SyncStatus.SYNCED.equals(parent.getSyncStatus());
            }
        };
    }
}

