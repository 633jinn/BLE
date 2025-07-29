package com.shinhan.ble

import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView
import no.nordicsemi.android.support.v18.scanner.ScanResult

/**
 * DeviceAdapter - BLE 스캔 결과를 RecyclerView에 표시하기 위한 어댑터
 * 
 * Nordic Semiconductor의 ScanResult를 사용하여 디바이스 정보를 표시합니다.
 * 각 디바이스의 이름, MAC 주소, RSSI 값을 보여줍니다.
 */
class DeviceAdapter(private val devices: List<ScanResult>) : 
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    /**
     * DeviceViewHolder - RecyclerView의 각 아이템 뷰를 관리
     * 
     * 디바이스 이름, MAC 주소, RSSI 값을 표시하는 TextView들을 포함합니다.
     */
    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.deviceName)      // 디바이스 이름
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress) // MAC 주소
        val deviceRssi: TextView = itemView.findViewById(R.id.deviceRssi)      // RSSI 값
    }

    /**
     * ViewHolder 생성
     * 
     * @param parent 부모 ViewGroup
     * @param viewType 뷰 타입 (사용하지 않음)
     * @return 생성된 DeviceViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        // item_device.xml 레이아웃을 인플레이트하여 ViewHolder 생성
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return DeviceViewHolder(view)
    }

    /**
     * ViewHolder에 데이터 바인딩
     * 
     * @param holder 바인딩할 ViewHolder
     * @param position 아이템 위치
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position]
        val context = holder.itemView.context
        
        // 디바이스 이름 설정 (이름이 없으면 "Unknown Device" 표시)
        holder.deviceName.text = device.device.name ?: context.getString(R.string.unknown_device)
        
        // MAC 주소 설정
        holder.deviceAddress.text = device.device.address
        
        // RSSI 값 설정 (신호 강도, 단위: dBm)
        holder.deviceRssi.text = context.getString(R.string.rssi_format, device.rssi)
    }

    /**
     * 아이템 개수 반환
     * 
     * @return 디바이스 목록의 크기
     */
    override fun getItemCount(): Int = devices.size
} 