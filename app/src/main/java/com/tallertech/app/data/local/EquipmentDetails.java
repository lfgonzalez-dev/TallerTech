package com.tallertech.app.data.local;

import androidx.room.Embedded;
import com.tallertech.app.data.local.entity.EquipmentEntity;

public class EquipmentDetails {
    @Embedded public EquipmentEntity equipment;
    public String clientName;
    public String orderStatus;
}
