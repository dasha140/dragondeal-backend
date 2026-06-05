package org.example.repository;

import org.example.domain.Wallet;
import org.example.domain.enums.BlockchainNetwork;
import org.example.domain.enums.WalletProvider;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "user"
    })
    List<Wallet> findAll();

    @EntityGraph(attributePaths = {
            "user"
    })
    Optional<Wallet> findWithUserById(Long id);

    @EntityGraph(attributePaths = {
            "user"
    })
    List<Wallet> findByUser_Id(Long userId);

    Optional<Wallet> findByWalletAddress(String walletAddress);

    boolean existsByWalletAddress(String walletAddress);

    List<Wallet> findByNetwork(BlockchainNetwork network);

    List<Wallet> findByProvider(WalletProvider provider);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Wallet w
            set w.walletAddress = :walletAddress,
                w.network = :network,
                w.provider = :provider
            where w.id = :id
            """)
    int updateWalletInfoById(@Param("id") Long id,
                             @Param("walletAddress") String walletAddress,
                             @Param("network") BlockchainNetwork network,
                             @Param("provider") WalletProvider provider);
}