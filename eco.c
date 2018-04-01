// NAME
//   eco.c - ecosystem
//     Entry for the ecosystem function.
// NOTES
//   None.
//
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "util.h"
#include "wm.h"
#include "pop.h"
#include "fit.h"

static T_BIAS notes     [NUM_NOTES+1];   // number of notes + pause
static T_BIAS velocities[NUM_VELOCITIES];
static T_BIAS durations [NUM_DURATIONS];

int  midi_soundcheck( T_PARAM * );

void initiate_environment( T_WORLD  *, T_PARAM * );
void diverge(              T_WORLD  *, T_PARAM * );
void organize(             T_WORLD  *, T_PARAM * );
void write_song(           T_WORLD  *, T_PARAM * );

void  adjust_ecosystem(    T_WORLD  *, T_PARAM * );
void enhance_ecosystem(    T_WORLD  *, T_PARAM * );
void express_genes(        T_SPECIE *, T_PARAM * );

void display_data(  int,   T_SPECIE *s, bool, T_PARAM *p );
void display_final( T_WORLD *,                     T_PARAM *p );

static T_WORLD    world;
//
// Genetic Evolution
//
int select_parent( int ispecies, T_LIFE *lives, T_PARAM *p );
void reproduce( int ispecies, T_LIFE *lives0, T_LIFE *lives1,
                int pa, int pb, int ci, T_PARAM *p );
void mutate( int ispecies, T_LIFE *lives1, int ci, T_PARAM *p );
//
// *** Ecosystem Entry *************************************************
//
int eco( T_PARAM *p )
{
  int i, j, parent_a, parent_b;
  T_SPECIE  *specie0, *specie1, *genswap;
 
  if( p->trace >= 3 ) write_out( "Start Ecosystem. . .\n");
  //
  p->world = &world;
  //
  // Create initial environmental conditions
  //
  initiate_environment( &world, p );
  //
  // Create initial population of one or more species
  //
  initiate_population( &world, p );
  //
  if( p->miditest ) return( midi_soundcheck( p ) );
  //
  // For each generation
  // 
  if( p->trace >= 3) write_out( "Start Evolution. . .\n");
  //
  for( p->igens = 0; p->igens < p->ngens; p->igens++ ) {
    //
    // Express genes of all organisms in one generation
    // Compute fitness for all species of the current generation.
    //
    express_genes( world.gen0, p );
    compute_fitness( &world,   p );
    if( fit_to_exit(           p )) break;
    //
    // Epigenetic Interventions
    //
    adjust_ecosystem( &world, p );
    //
    // For each active species of the generation
    //
    for( i = 0; i < p->nspecies; i++) {
     
      if( p->species[i].active ) {

        specie0 = &(world.gen0[i]);   // current generation
        specie1 = &(world.gen1[i]);   // new    generation

        display_data( i, specie0, FALSE, p );
        //
        // The number of reproduction is half the number of life.
        // In each loop, select 2 parents to produce 2 offspring.
        //
        for( j = 0; j < p->species[i].nlives; j += 2 ) {
          //
          // Select two parents by fitness.
          //
          parent_a = select_parent( i, specie0->lives, p );
          parent_b = select_parent( i, specie0->lives, p );

          if( p->trace >= 5 ) {
            sprintf( str, "str, Parents: %i %i\n", parent_a, parent_b );
            write_out( str );
          }
          //
          // Reproduce two children conditioned on crossover rate.
          //
          reproduce( i, specie0->lives, specie1->lives,
                        parent_a, parent_b, j, p );
          //
          // Mutate offspring conditioned on mutation rate.
          //
          mutate( i, specie1->lives, j, p );
        }                       // For every 2 organisms
        // express_genes( specie1, p );
      }                         // If active species
    }                           // For all species
    // 
    // Diverge new species
    //
//  list_nucs( "DIV1", specie1, 0, 0, p );
    diverge( &world, p );
//  list_nucs( "DIV2", specie1, 0, 0, p );
    //
    // Organize Community
    // 
//  list_nucs( "ORG1", specie1, 0, 0, p );
    organize( &world, p );
//  list_nucs( "ORG2", specie1, 0, 0, p );
    //
    // Swap new with old generation of lives.
    // Just swap the address.
    //
    // Need to keep same species parameters between 2 generations in sync
    // Can use a different array of species to keep track. 
    //
    genswap    = world.gen1;
    world.gen1 = world.gen0;
    world.gen0 = genswap;
    for( i = 0; i < p->nspecies; i++ ) p->species[i].sp = &(world.gen0[i]);
  }                              // For all generations
  //
  // End of ecosystem evolution.
  //   - Get latest fittest information.
  //   - Write song to file in MIDI format.
  // Note that after the swap, gen0 is the current generation.
  //
//  write_out(  "Express Genes for the Fittest. . .\n" );
//  write_out(  "Expressed!\n" );

  if( p->igens >= p->ngens ) {
//  express_genes_fittest( &(world.gen0[i]), p );
    express_genes( world.gen0, p );
    compute_fitness ( &world, p );
  }
  enhance_ecosystem( &world, p );
  for( i = 0; i < p->nspecies; i++) {
    if( p->species[i].active ) {
      display_data(  i, &(world.gen0[i]), TRUE, p );
    }
  }
  write_song( &world, p );
  display_final( &world, p );
  write_out( "Completed.\n" );
  // free all fittest organisms of each species
  // free species
  // purge_population( &world, p ); - crashes
  free( world.gen0 );
  free( world.gen1 );
  free( p->species );
  fclose( p->fp );
  return( 1 );
}

