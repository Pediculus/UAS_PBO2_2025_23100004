/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject4;

/**
 *
 * @author ITBSS
 */
import graphql.*;
import graphql.schema.idl.*;
import java.io.*;
import java.util.Objects;
import graphql.schema.GraphQLSchema;

public class GraphQLConfig {
   public static GraphQL init() throws IOException {
       InputStream schemaStream = GraphQLConfig.class.getClassLoader().getResourceAsStream("schema.graphqls");
       
       if (schemaStream == null) {
           throw new RuntimeException("schema.graphqls not found in classpath.");
       }
       
       String schema = new String(Objects.requireNonNull(schemaStream).readAllBytes());

       TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);
       RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
           .type("Query", builder -> builder
               .dataFetcher("allLog", env -> VisitRepository.findAll())
               .dataFetcher("logById", env -> {
                   Long id = env.getArgument("id");
                   return VisitRepository.findById(id);
               })
           )
           .type("Mutation", builder -> builder
               .dataFetcher("addLog", env -> VisitRepository.add(
                   env.getArgument("studentName"),
                   env.getArgument("studentId"),
                   env.getArgument("studyProgram"),
                   env.getArgument("purpose")

               ))
               .dataFetcher("deleteLog", env -> {
                   Long id = Long.parseLong(env.getArgument("id").toString());
                   return VisitRepository.delete(id);
               })

//               .dataFetcher("editProduct", env -> {
//                   Long id = Long.parseLong(env.getArgument("id").toString());
//                   String name = env.getArgument("name");
//                   Double price = env.getArgument("price");
//                   String category = env.getArgument("category");
//                   
//                   VisitLog product = VisitRepository.findById(id);
//                   if(product == null) return null;
//                   
//                   product.setName(name);
//                   product.setPrice(price);
//                   product.setCategory(category);
//                   return product;
//               })
           )
           .build();

       GraphQLSchema schemaFinal = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
       return GraphQL.newGraphQL(schemaFinal).build();
   }
}