package com.telecom.Wezen.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telecom.Wezen.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {
	List<Plan>  findByPlanType(String planType);

}
