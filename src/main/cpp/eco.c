// NAME
//   eco.c - ecosystem
//     Entry for music ecosystem development.
// NOTES
//   None.
//
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include "util.h"
#include "wm.h"
#include "fit.h"
//
int  midi_soundcheck(                 T_PARAM * );
int  write_song(          T_WORLD  *, T_PARAM * );
//
void genesis(             T_WORLD  *, T_PARAM * );
void apocalypse(          T_WORLD  *, T_PARAM * );
// Genetic Evolution
int  select_parent( int,           T_LIFE *,             char, T_PARAM * );
void crossover(     int, T_LIFE *, T_LIFE *, T_PARENTS *, int, T_PARAM * );
void mutate(        int,           T_LIFE *,              int, T_PARAM * );
// Epigenetics
void adjust_ecosystem(    T_WORLD  *, T_PARAM * );
void enhance_ecosystem(   T_WORLD  *, T_PARAM * );
int  express_genes(       T_SPECIE *, T_PARAM * );
// Self Organization
int  diverge(             T_WORLD  *, T_PARAM * );
int  organize(            T_WORLD  *, T_PARAM * );
//
void display_final(       T_WORLD  *,       T_PARAM *p );
void display_data(  int,  T_SPECIE *, bool, T_PARAM *p );
//
static T_WORLD    world;
//
// *** Ecosystem Entry *************************************************
//
int eco( T_PARAM *p )
{
  int i, j, k;
  T_PARENTS  parents;
  T_SPECIE  *specie0, *specie1, *genswap;

  if( p->trace >= 0 ) Print( "Initiating Ecosystem. . .\n");
  //
  // Create world
  // Define environment, and
  // Create population of one or more species.
  //
  genesis( &world, p );
  //
  if( p->miditest ) return( midi_soundcheck( p ) );
  //
  // Evolve
  // 
  if( p->trace >= 0 ) Print( "Starting Evolution. . .\n");
  //
  for( p->igens = 0; p->igens < p->ngens; p->igens++ ) {
    //
    // Express genes of all organisms in one generation
    // Compute fitness for all species of the current generation.
    //
    if( express_genes( world.gen0, p ) < NORMAL ) break;
    compute_fitness( &world,   p );
    if( fit_to_exit(           p ) ) break;
    //
    // Epigenetic Interventions
    //
    adjust_ecosystem( &world, p );
    //
    // Evolve the population of species.
    // Some species may diverge after initial evolution.
    //
    for( i = 0; i < p->nspecies; i++) {
		//Print("Specie %d'n", i);
     
      if( p->species[i].active ) {

        specie0 = &(world.gen0[i]);   // current generation
        specie1 = &(world.gen1[i]);   // new     generation

        display_data( i, specie0, FALSE, p );
        //
        // The number of reproduction is half the number of lives.
        // In each loop, select 2 parents to produce 2 offsprings.
        //
        for( j = 0; j < p->species[i].nlives; j += 2 ) {
          //
          // Select two parents by fitness.
          //
          parents.ak = select_parent( i, specie0->lives, 'K', p );
          parents.ad = select_parent( i, specie0->lives, 'D', p );
          parents.bk = select_parent( i, specie0->lives, 'K', p );
          parents.bd = select_parent( i, specie0->lives, 'D', p );
          //
          // Reproduce by crossover of 2 children conditioned on crossover rate
          //
          crossover( i, specie0->lives, specie1->lives, &parents, j, p );
          //
          // Mutate offspring conditioned on mutation rate.
          //
          mutate( i, specie1->lives, j, p );
        }                       // For every 2 organisms
      }                         // If active species
    }                           // For all species
    // 
    // Diverge new species  - organize before diverge?
    //
    if( diverge( &world, p ) < NORMAL ) break;
    //
    // Organize Community
    //
    if( organize( &world, p ) < NORMAL ) break;
    //
    // Swap new with old generation of organisms for all species.
    //
    genswap    = world.gen1;
    world.gen1 = world.gen0;
    world.gen0 = genswap;
    for( i = 0; i < p->nspecies; i++ ) {
      p->species[i].sp = &(world.gen0[i]);
    }
  }                              // For all generations
  if( p->status >= NORMAL ) {
    //
    // End of ecosystem evolution.
    //   - Get final fittest information.
    //   - Write song to file in MIDI format.
    // Note that after the swap, gen0 is the current generation.
    //
    if (p->igens >= p->ngens) {
        if (express_genes(world.gen0, p) < NORMAL) goto end_of_eco;
        compute_fitness(&world, p);
    }
    enhance_ecosystem(&world, p);
    for( k = 0; k < p->nspecies; k++ ) {
      if( p->species[k].active ) {
        display_data(k, &(world.gen0[k]), TRUE, p);
      }
    }
    if( write_song(&world, p) >= NORMAL ) {
      display_final(&world, p);
      Print("Completed.\n");
    } else {
      Print("MIDI file not written (%d).\n", p->status );
    }
  }
  end_of_eco:
  // Destroy world -- population and environment
  apocalypse( &world, p );
  return( p->status );
}
