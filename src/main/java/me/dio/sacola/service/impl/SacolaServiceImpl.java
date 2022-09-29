package me.dio.sacola.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.resource.dto.ItemDto;
import me.dio.sacola.service.SacolaService;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;

@Service
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
    private final SacolaRepository sacolaRepository;
    private final ProdutoRepository produtoRepository;

    @Override
    public Item incluirItemNaSacola(ItemDto ItemDto) {
        Sacola sacola = verSacola(ItemDto.getSacolaId());

        if (sacola.isFechada()) {
            throw new RuntimeException("Esta Sacola está fechada");
        }

        Item itemParaSerInserido = Item.builder()
                .qunatidade(ItemDto.getQuantidade())
                .sacola(sacola)
                .produto(produtoRepository.findById(ItemDto.getProdutoId()).orElseThrow(
                        () -> {
                            throw new RuntimeException("Esse produto não existe");
                        }))
                .build();

        List<Item> itensDaSacola = sacola.getItens();
        if (itensDaSacola.isEmpty()) {
            itensDaSacola.add(itemParaSerInserido);
        } else {
            Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
            Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();
            if (restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
                itensDaSacola.add(itemParaSerInserido);
            } else {
                throw new RuntimeException(
                        "So é possivel adicionar produtos de restaurantes diferentes. Feche a sacola ou esvazie!");
            }
        }

        return null;
    }

    @Override
    public Sacola verSacola(Long id) {
        return sacolaRepository.findById(id).orElseThrow(
                () -> {
                    throw new RuntimeException("Essa sacola não existe!");
                });
    }

    @Override
    public Sacola fecharSacola(Long id, int numeroformaPagamneto) {
        Sacola sacola = verSacola(id);

        if (sacola.getItens().isEmpty()) {
            throw new RuntimeException("Inclua itens na sacola!");
        }

        FormaPagamento formaPagamento = numeroformaPagamneto == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;

        sacola.setFormaPagamento(formaPagamento);
        sacola.setFechada(true);
        return sacolaRepository.save(sacola);
    }

}