package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.example.demo.pojo.Memo;

/**
 * SQLite MemoRepository 适配器
 * 包装 MyBatis MemoRepository 并实现 MemoRepositoryInterface
 * 
 * @author Lepeng Zhou
 * @version 1.0
 */
@Repository("sqliteMemoRepositoryAdapter")
public class SqliteMemoRepositoryAdapter implements MemoRepositoryInterface {
    
    @Autowired
    @Qualifier("sqliteMemoRepository")
    private MemoRepository memoRepository;
    
    @Override
    public Memo save(Memo memo) {
        if (memo.getId() == null) {
            // 新记录，使用insert
            int result = memoRepository.insert(memo);
            if (result > 0) {
                return memo;
            }
        } else {
            // 更新记录，使用update
            int result = memoRepository.update(memo);
            if (result > 0) {
                return memo;
            }
        }
        return null;
    }
    
    @Override
    public Optional<Memo> findById(Long id) {
        return memoRepository.findById(id);
    }
    
    @Override
    public Optional<Memo> findByUserIdAndId(Long userId, Long memoId) {
        return memoRepository.findByUserIdAndId(userId, memoId);
    }
    
    @Override
    public List<Memo> findByUserId(Long userId) {
        return memoRepository.findByUserId(userId);
    }
    
    @Override
    public List<Memo> findByUserIdAndType(Long userId, String type) {
        return memoRepository.findByUserIdAndType(userId, type);
    }
    
    @Override
    public List<Memo> findImportantByUserId(Long userId) {
        return memoRepository.findImportantByUserId(userId);
    }
    
    @Override
    public List<Memo> findByUserIdAndPinCode(Long userId, String pinCode) {
        return memoRepository.findByUserIdAndPinCode(userId, pinCode);
    }
    
    @Override
    public List<Memo> searchByKeyword(Long userId, String keyword) {
        return memoRepository.searchByKeyword(userId, keyword);
    }
    
    @Override
    public int softDelete(Long userId, Long memoId) {
        return memoRepository.softDelete(userId, memoId);
    }
    
    @Override
    public long countByUserId(Long userId) {
        return memoRepository.countByUserId(userId);
    }
    
    @Override
    public long countImportantByUserId(Long userId) {
        return memoRepository.countImportantByUserId(userId);
    }
    
    @Override
    public long countByUserIdAndType(Long userId, String type) {
        return memoRepository.countByUserIdAndType(userId, type);
    }
    
    @Override
    public boolean existsByUserIdAndPinCode(Long userId, String pinCode) {
        return memoRepository.existsByUserIdAndPinCode(userId, pinCode);
    }
    
    @Override
    public List<Memo> findAll() {
        return memoRepository.findAll();
    }
    
    @Override
    public long count() {
        return memoRepository.count();
    }
} 
 
 