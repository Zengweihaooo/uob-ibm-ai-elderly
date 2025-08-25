package com.example.demo.mapper;

import com.example.demo.pojo.ImportantDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ImportantDateMapper {
	int insert(ImportantDate importantDate);

	int update(ImportantDate importantDate);

	int deleteById(@Param("id") Long id);

	ImportantDate findById(@Param("id") Long id);

	List<ImportantDate> findByUserId(@Param("userId") Long userId);

	List<ImportantDate> findByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);

	int setEnabled(@Param("id") Long id, @Param("enabled") boolean enabled, @Param("updatedAt") LocalDateTime updatedAt);

	int updateWeekReminderSent(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);

	int updateDayReminderSent(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);

	int existsByUserTitleDateType(@Param("userId") Long userId,
								  @Param("title") String title,
								  @Param("date") java.time.LocalDate date,
								  @Param("type") String type);
	
	/**
	 * Find all important dates
	 * @return List of all important dates
	 */
	@Select("SELECT * FROM important_dates")
	List<ImportantDate> findAll();
	
	/**
	 * Count total number of important dates
	 * @return Total number of important dates
	 */
	@Select("SELECT COUNT(*) FROM important_dates")
	long count();
}


