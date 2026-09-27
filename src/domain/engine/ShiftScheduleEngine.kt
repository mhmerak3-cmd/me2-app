package com.me2.app.domain.engine

import java.time.LocalTime

enum class ShiftType {
    MORNING, EVENING, NIGHT, ROTATING, REST_DAY
}

data class ShiftConfig(
    val shiftName: String,
    val shiftType: ShiftType,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class ScheduleBlock(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val title: String,
    val description: String,
    val isWorkDuty: Boolean = false
)

class ShiftScheduleEngine {

    fun generateTimeline(
        wakeUpTime: LocalTime,
        sleepTargetTime: LocalTime,
        activeShift: ShiftConfig?
    ): List<ScheduleBlock> {
        val blocks = mutableListOf<ScheduleBlock>()

        // রেস্ট ডে বা কোনো শিফট না থাকলে সাধারণ রুটিন
        if (activeShift == null || activeShift.shiftType == ShiftType.REST_DAY) {
            blocks.add(ScheduleBlock(wakeUpTime, wakeUpTime.plusMinutes(15), "ঘুম থেকে উঠা ও হাইড্রেশন", "৫০০ মিলি পানি + সূর্যের আলো"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(15), wakeUpTime.plusMinutes(45), "সকালের হাইজিন", "ব্রাশ, ফ্রেশ হওয়া ও গ্রুমিং"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(45), wakeUpTime.plusMinutes(105), "ওয়ার্কআউট সেশন", "ব্যায়াম ও স্ট্রেচিং"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(105), wakeUpTime.plusMinutes(150), "সকালের নাস্তা", "উচ্চ প্রোটিনযুক্ত খাবার"))
            blocks.add(ScheduleBlock(LocalTime.of(13, 0), LocalTime.of(14, 0), "লার্নিং সেশন", "বই পড়া বা স্কিল ডেভেলপমেন্ট"))
            blocks.add(ScheduleBlock(LocalTime.of(20, 0), LocalTime.of(21, 0), "রাতের খাবার", "পুষ্টিকর ব্যালান্সড মিল"))
            blocks.add(ScheduleBlock(sleepTargetTime.minusMinutes(45), sleepTargetTime, "ঘুমের প্রস্তুতি", "স্ক্রিন বন্ধ রাখা ও রিলাক্সেশন"))
            return blocks.sortedBy { it.startTime }
        }

        val dutyStart = activeShift.startTime
        val dutyEnd = activeShift.endTime
        val isEveningShift = dutyStart.isAfter(LocalTime.of(13, 0)) || dutyStart == LocalTime.of(13, 0)

        if (isEveningShift) {
            // ইভনিং শিফট: যেমন ১৫:০০ থেকে ০০:০০ (ডিউটির আগেই ওয়ার্কআউট)
            blocks.add(ScheduleBlock(wakeUpTime, wakeUpTime.plusMinutes(15), "ঘুম থেকে উঠা", "পানি পান ও রিফ্রেশ"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(20), wakeUpTime.plusMinutes(80), "প্রি-ডিউটি ওয়ার্কআউট", "ডিউটির আগেই ব্যায়াম সম্পন্ন করা"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(80), wakeUpTime.plusMinutes(110), "গোসল ও গ্রুমিং", "পরিষ্কার পোশাক ও প্রস্তুতি"))
            blocks.add(ScheduleBlock(wakeUpTime.plusMinutes(110), wakeUpTime.plusMinutes(150), "পুষ্টিকর খাবার", "ডিউটির আগের ভারী মিল"))
            blocks.add(ScheduleBlock(dutyStart, dutyEnd, "ডিউটি: ${activeShift.shiftName}", "কর্মক্ষেত্রে মূল ডিউটি", isWorkDuty = true))
            blocks.add(ScheduleBlock(dutyEnd.plusMinutes(20), dutyEnd.plusMinutes(50), "পোস্ট-ডিউটি রিলাক্স", "হালকা স্ন্যাক ও রিল্যাক্স"))
            blocks.add(ScheduleBlock(sleepTargetTime.minusMinutes(30), sleepTargetTime, "ঘুম", "পর্যাপ্ত বিশ্রাম"))
        } else {
            // মর্নিং শিফট: যেমন ০৬:০০ থেকে ১৫:০০ (ডিউটির পরে ওয়ার্কআউট)
            blocks.add(ScheduleBlock(wakeUpTime, wakeUpTime.plusMinutes(20), "ভোরে উঠা ও প্রস্তুতি", "পানি ও দ্রুত ফ্রেশ হওয়া"))
            blocks.add(ScheduleBlock(dutyStart, dutyEnd, "ডিউটি: ${activeShift.shiftName}", "কর্মক্ষেত্রে মূল ডিউটি", isWorkDuty = true))
            blocks.add(ScheduleBlock(dutyEnd.plusMinutes(45), dutyEnd.plusMinutes(105), "পোস্ট-ডিউটি ওয়ার্কআউট", "শরীর সক্রিয় থাকা অবস্থায় ব্যায়াম"))
            blocks.add(ScheduleBlock(dutyEnd.plusMinutes(105), dutyEnd.plusMinutes(135), "গোসল ও ফ্রেশ হওয়া", "ক্লান্তি কাটানোর সেশন"))
            blocks.add(ScheduleBlock(LocalTime.of(19, 30), LocalTime.of(20, 30), "রাতের খাবার ও ট্র্যাকিং", "দিনের খাবার ও কাজের হিসাব"))
            blocks.add(ScheduleBlock(sleepTargetTime.minusMinutes(45), sleepTargetTime, "ঘুমের প্রস্তুতি", "পরের দিনের মর্নিং শিফটের জন্য দ্রুত ঘুমানো"))
        }

        return blocks.sortedBy { it.startTime }
    }
}
