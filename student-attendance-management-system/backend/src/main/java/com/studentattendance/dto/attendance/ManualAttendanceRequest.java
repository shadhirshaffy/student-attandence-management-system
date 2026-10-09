package com.studentattendance.dto.attendance;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class ManualAttendanceRequest {

    @NotEmpty
    private List<@Valid ManualAttendanceItemRequest> records = new ArrayList<>();

    public List<@Valid ManualAttendanceItemRequest> getRecords() {
        return records;
    }

    public void setRecords(List<@Valid ManualAttendanceItemRequest> records) {
        this.records = records;
    }
}
