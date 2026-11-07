package com.tallertech.app.data.local;

import androidx.room.Embedded;
import com.tallertech.app.data.local.entity.ServiceOrderEntity;

public class OrderDetails {
    @Embedded public ServiceOrderEntity order;
    public String clientName;
    public String equipmentName;
}
